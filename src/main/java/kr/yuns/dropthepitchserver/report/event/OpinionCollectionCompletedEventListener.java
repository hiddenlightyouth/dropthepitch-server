package kr.yuns.dropthepitchserver.report.event;

import kr.yuns.dropthepitchserver.opinion.event.OpinionCollectionCompletedEvent;
import kr.yuns.dropthepitchserver.report.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


//@Async을 처리
@Component
@Slf4j
@RequiredArgsConstructor
public class OpinionCollectionCompletedEventListener {

    private final ReportGenerationService reportGenerationService;

    @Async
    @EventListener
    public void onOpinionCollectionCompleted(OpinionCollectionCompletedEvent event) {
        log.info("[onOpinionCollectionCompleted] 리포트 생성 트리거: projectId={}", event.projectId());

        try {
            reportGenerationService.generate(event.projectId());
        } catch (Exception e) {
            log.error("[onOpinionCollectionCompleted] 리포트 생성 실패: projectId={}", event.projectId(), e);
        }
    }
}
