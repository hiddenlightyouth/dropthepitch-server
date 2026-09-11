package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.FileAnalysisResult;
import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.entity.AnalysisTimeline;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.exception.AnalysisNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.exception.FileNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import kr.yuns.dropthepitchserver.analyze.event.AnalysisCompletedEvent;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.regex.Pattern;

//DB 작업만 담당한다. AI 호출처럼 오래 걸리는 일은 여기에 두지 않는다.
//트랜잭션을 짧게 유지해 커넥션을 오래 잡지 않기 위해 FileAnalysisService와 분리했다.
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultService {

    private static final int TIMELINE_CONTENT_MAX_LENGTH = 500;
    private static final int TITLE_MAX_LENGTH = 100;
    private static final Pattern REPEATED_TEXT = Pattern.compile("(자막|화면 글자): ([^/]+?) / 음성: \\2\\.?(?= /|$)");

    private final AnalysisRepository analysisRepository;
    private final FileRepository fileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 분석에 필요한 파일 정보를 읽어옵니다.
     *
     * @param projectId 프로젝트 ID
     * @return 파일
     */
    @Transactional(readOnly = true)
    public File getFile(Long projectId) {
        return fileRepository.findByProjectId(projectId)
                .orElseThrow(FileNotFoundException::new);
    }

    /**
     * 분석 결과를 저장하고 상태를 완료로 바꿉니다.
     * 영상이면 타임라인 구간도 함께 저장하고, 작업 이름을 AI가 지은 제목으로 바꿉니다.
     *
     * @param projectId 프로젝트 ID
     * @param callResult AI 호출 결과
     * @param videoSeconds 영상 길이(초). 영상이 아니거나 읽지 못했으면 null
     */
    @Transactional
    public void saveSuccess(Long projectId, AnalysisCallResult callResult, Integer videoSeconds) {
        FileAnalysisResult result = callResult.result();
        Analysis analysis = getAnalysis(projectId);

        //불법 콘텐츠면 페르소나 선별 이벤트 없이 끝낸다.
        FileAnalysisResult.PolicyViolation violation = result.policyViolation();
        if (violation != null && violation.violated()) {
            analysis.reject(violation.category());
            analysis.getProject().fail();
            log.warn("[saveSuccess] 불법 콘텐츠로 분석을 거절합니다: projectId={}, 분류={}, 근거={}",
                    projectId, violation.category(), violation.evidence());
            return;
        }

        //detail에는 모델이 돌려준 원본 JSON을 넣는다.
        //객체로 다시 만들면 record에 없는 필드가 사라져, 스키마를 고칠 때마다 자바도 고쳐야 한다.
        analysis.complete(result.summary(), withoutPolicyViolation(callResult.rawJson()));

        if (result.timeline() != null) {
            result.timeline().stream()
                    .filter(this::isValidSegment)
                    .filter(segment -> isWithinVideo(projectId, segment, videoSeconds))
                    .forEach(segment -> analysis.addAnalysisTimeline(AnalysisTimeline.builder()
                            .analysis(analysis)
                            .startTime(segment.startTime())
                            .endTime(videoSeconds == null ? segment.endTime() : Math.min(segment.endTime(), videoSeconds))
                            .content(truncate(tidy(segment.content())))
                            .build()));
        }

        applyAiTitle(analysis.getProject(), result.title());

        //커밋이 끝난 뒤 페르소나 선별 이벤트 발행
        eventPublisher.publishEvent(new AnalysisCompletedEvent(projectId));

        //analysis.getAnalysisTimelines()를 세면 지연 로딩이 깨어나 쿼리가 한 번 더 나간다.
        log.info("[saveSuccess] 분석 결과 저장: projectId={}, detail={}자, timeline={}건",
                projectId,
                callResult.rawJson() == null ? 0 : callResult.rawJson().length(),
                result.timeline() == null ? 0 : result.timeline().size());
    }

    /**
     * 분석 실패를 기록합니다. 화면이 계속 '분석중'에 머물지 않게 합니다.
     *
     * @param projectId 프로젝트 ID
     */
    @Transactional
    public void saveFailure(Long projectId) {
        Analysis analysis = getAnalysis(projectId);
        analysis.fail();
        analysis.getProject().fail();
        log.info("[saveFailure] 분석 실패 기록: projectId={}", projectId);
    }

    private Analysis getAnalysis(Long projectId) {
        return analysisRepository.findByProjectId(projectId)
                .orElseThrow(AnalysisNotFoundException::new);
    }

    //사이드바에 파일명 대신 AI가 지은 제목이 보이게 한다.
    private void applyAiTitle(Project project, String title) {
        //project.title은 not null이다. 여기서 비우면 분석 결과 전체가 롤백되므로 파일명을 그대로 둔다.
        if (!StringUtils.hasText(title)) {
            log.warn("[saveSuccess] AI 제목이 비어 파일명을 유지합니다: projectId={}", project.getId());
            return;
        }

        //project.title은 varchar(255)라 넘치면 저장 시점에 예외가 난다.
        project.changeTitle(title.length() <= TITLE_MAX_LENGTH
                ? title
                : title.substring(0, TITLE_MAX_LENGTH));
    }

    //세 컬럼 모두 not null이고 endTime이 startTime보다 커야 한다. 어긋난 구간은 저장하지 않는다.
    private boolean isValidSegment(FileAnalysisResult.TimelineSegment segment) {
        boolean valid = segment.startTime() != null
                && segment.endTime() != null
                && segment.content() != null
                && segment.endTime() > segment.startTime();

        if (!valid) {
            log.warn("[saveSuccess] 잘못된 타임라인 구간을 건너뜁니다: {}", segment);
        }
        return valid;
    }

    //영상 길이보다 뒤에서 시작하는 구간은 버린다.
    private boolean isWithinVideo(Long projectId, FileAnalysisResult.TimelineSegment segment, Integer videoSeconds) {
        if (videoSeconds == null || segment.startTime() < videoSeconds) {
            return true;
        }
        log.warn("[saveSuccess] 영상 길이를 넘는 구간을 건너뜁니다: projectId={}, 영상={}초, 구간={}~{}초",
                projectId, videoSeconds, segment.startTime(), segment.endTime());
        return false;
    }

    //모델이 같은 문장을 '화면 글자: A / 음성: A'처럼 두 번 쓰거나 '음성: 없음'을 붙이는 경우가 있어 저장 전에 정리한다.
    private static String tidy(String content) {
        return REPEATED_TEXT.matcher(content.replace(" / 음성: 없음", "")).replaceAll("$1·음성: $2");
    }

    //detail은 페르소나 브리프로 그대로 넘어가므로 거절 판정 필드는 남기지 않는다.
    private String withoutPolicyViolation(String rawJson) {
        ObjectNode root = (ObjectNode) objectMapper.readTree(rawJson);
        root.remove("policyViolation");
        return objectMapper.writeValueAsString(root);
    }

    //analysis_timeline.content는 varchar(500)이라 넘치면 저장 시점에 예외가 난다.
    private String truncate(String content) {
        return content.length() <= TIMELINE_CONTENT_MAX_LENGTH
                ? content
                : content.substring(0, TIMELINE_CONTENT_MAX_LENGTH);
    }
}
