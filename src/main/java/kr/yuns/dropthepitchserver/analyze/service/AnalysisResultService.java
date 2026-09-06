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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//DB 작업만 담당한다. AI 호출처럼 오래 걸리는 일은 여기에 두지 않는다.
//트랜잭션을 짧게 유지해 커넥션을 오래 잡지 않기 위해 FileAnalysisService와 분리했다.
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultService {

    private static final int TIMELINE_CONTENT_MAX_LENGTH = 500;

    private final AnalysisRepository analysisRepository;
    private final FileRepository fileRepository;
    private final ApplicationEventPublisher eventPublisher;

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
     * 영상이면 타임라인 구간도 함께 저장합니다.
     *
     * @param projectId 프로젝트 ID
     * @param callResult AI 호출 결과
     */
    @Transactional
    public void saveSuccess(Long projectId, AnalysisCallResult callResult) {
        FileAnalysisResult result = callResult.result();
        Analysis analysis = getAnalysis(projectId);

        //detail에는 모델이 돌려준 원본 JSON을 그대로 넣는다.
        //객체로 다시 만들면 record에 없는 필드가 사라져, 스키마를 고칠 때마다 자바도 고쳐야 한다.
        analysis.complete(result.summary(), callResult.rawJson());

        if (result.timeline() != null) {
            result.timeline().stream()
                    .filter(this::isValidSegment)
                    .forEach(segment -> analysis.addAnalysisTimeline(AnalysisTimeline.builder()
                            .analysis(analysis)
                            .startTime(segment.startTime())
                            .endTime(segment.endTime())
                            .content(truncate(segment.content()))
                            .build()));
        }
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
        getAnalysis(projectId).fail();
        log.info("[saveFailure] 분석 실패 기록: projectId={}", projectId);
    }

    private Analysis getAnalysis(Long projectId) {
        return analysisRepository.findByProjectId(projectId)
                .orElseThrow(AnalysisNotFoundException::new);
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

    //analysis_timeline.content는 varchar(500)이라 넘치면 저장 시점에 예외가 난다.
    private String truncate(String content) {
        return content.length() <= TIMELINE_CONTENT_MAX_LENGTH
                ? content
                : content.substring(0, TIMELINE_CONTENT_MAX_LENGTH);
    }
}
