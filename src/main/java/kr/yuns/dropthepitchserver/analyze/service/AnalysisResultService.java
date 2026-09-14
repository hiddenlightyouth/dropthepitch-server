package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.FileAnalysisResult;
import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.entity.AnalysisTimeline;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultService {

    private static final int TIMELINE_CONTENT_MAX_LENGTH = 500;
    private static final int TITLE_MAX_LENGTH = 100;
    private static final Pattern CLOCK = Pattern.compile("(?:(\\d+):)?(\\d{1,2}):([0-5]\\d)");
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

        AnalysisVerdict verdict = result.review() == null ? null : AnalysisVerdict.from(result.review().verdict());

        if (verdict == null) {
            analysis.fail(AnalysisVerdict.AI_ERROR);
            analysis.getProject().fail();
            log.error("[saveSuccess] 판정을 읽을 수 없어 실패로 남깁니다: projectId={}, review={}", projectId, result.review());
            return;
        }

        if (verdict != AnalysisVerdict.ANALYZABLE) {
            reject(analysis, verdict, result.review().reason());
            return;
        }

        analysis.complete(result.summary(), withoutInternalFields(callResult.rawJson()));

        if (result.timeline() != null) {
            result.timeline().forEach(segment -> addTimeline(projectId, analysis, segment, videoSeconds));
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
     * AI를 부르기 전에 서버가 판단한 거절을 기록합니다.
     *
     * @param projectId 프로젝트 ID
     * @param verdict 거절 판정
     */
    @Transactional
    public void saveRejected(Long projectId, AnalysisVerdict verdict) {
        reject(getAnalysis(projectId), verdict, "서버 확인");
    }

    private void reject(Analysis analysis, AnalysisVerdict verdict, String reason) {
        analysis.fail(verdict);
        analysis.getProject().fail();
        log.warn("[reject] 분석을 거절합니다: projectId={}, 판정={}, 근거={}",
                analysis.getProject().getId(), verdict.getLabel(), reason);
    }

    /**
     * 분석 실패를 기록합니다. 화면이 계속 '분석중'에 머물지 않게 합니다.
     *
     * @param projectId 프로젝트 ID
     * @param reason 실패 사유
     */
    @Transactional
    public void saveFailure(Long projectId, AnalysisVerdict reason) {
        Analysis analysis = getAnalysis(projectId);
        analysis.fail(reason);
        analysis.getProject().fail();
        log.info("[saveFailure] 분석 실패 기록: projectId={}, 사유={}", projectId, reason);
    }

    private Analysis getAnalysis(Long projectId) {
        return analysisRepository.findByProjectId(projectId)
                .orElseThrow(AnalysisNotFoundException::new);
    }

    private void applyAiTitle(Project project, String title) {
        //project.title은 not null이다. 여기서 비우면 분석 결과 전체가 롤백되므로 파일명을 그대로 둔다.
        if (!StringUtils.hasText(title)) {
            log.warn("[saveSuccess] AI 제목이 비어 파일명을 유지합니다: projectId={}", project.getId());
            return;
        }

        project.changeTitle(title.length() <= TITLE_MAX_LENGTH
                ? title
                : title.substring(0, TITLE_MAX_LENGTH));
    }

    private void addTimeline(Long projectId, Analysis analysis, FileAnalysisResult.TimelineSegment segment, Integer videoSeconds) {
        Integer start = toSeconds(segment.startTime());
        Integer end = toSeconds(segment.endTime());

        if (start == null || end == null || segment.content() == null || end <= start
                || (videoSeconds != null && start >= videoSeconds)) {
            log.warn("[saveSuccess] 잘못된 타임라인 구간을 건너뜁니다: projectId={}, 영상={}초, 구간={}",
                    projectId, videoSeconds, segment);
            return;
        }

        analysis.addAnalysisTimeline(AnalysisTimeline.builder()
                .analysis(analysis)
                .startTime(start)
                .endTime(videoSeconds == null ? end : Math.min(end, videoSeconds))
                .content(truncate(tidy(segment.content())))
                .build());
    }

    private static Integer toSeconds(String clock) {
        Matcher matcher = clock == null ? null : CLOCK.matcher(clock.trim());
        if (matcher == null || !matcher.matches()) {
            return null;
        }
        int hours = matcher.group(1) == null ? 0 : Integer.parseInt(matcher.group(1));
        return hours * 3600 + Integer.parseInt(matcher.group(2)) * 60 + Integer.parseInt(matcher.group(3));
    }

    private static String tidy(String content) {
        return REPEATED_TEXT.matcher(content.replace(" / 음성: 없음", "")).replaceAll("$1·음성: $2");
    }

    private String withoutInternalFields(String rawJson) {
        ObjectNode root = (ObjectNode) objectMapper.readTree(rawJson);
        root.remove("review");
        return objectMapper.writeValueAsString(root);
    }

    private String truncate(String content) {
        return content.length() <= TIMELINE_CONTENT_MAX_LENGTH
                ? content
                : content.substring(0, TIMELINE_CONTENT_MAX_LENGTH);
    }
}
