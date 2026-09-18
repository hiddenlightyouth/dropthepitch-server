package kr.yuns.dropthepitchserver.persona.event;

import kr.yuns.dropthepitchserver.analyze.event.AnalysisCompletedEvent;
import kr.yuns.dropthepitchserver.persona.service.PersonaMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@Slf4j
@RequiredArgsConstructor
public class AnalysisCompletedEventListener {

    private final PersonaMatchingService personaMatchingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnalysisCompleted(AnalysisCompletedEvent event) {
        log.info("[onAnalysisCompleted] 페르소나 선별 트리거: projectId={}", event.projectId());

        try {
            personaMatchingService.match(event.projectId());
        } catch (Exception e) {
            log.error("[onAnalysisCompleted] 페르소나 선별 실패: projectId={}", event.projectId(), e);
        }
    }
}
