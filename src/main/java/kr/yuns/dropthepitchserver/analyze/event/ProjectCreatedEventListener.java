package kr.yuns.dropthepitchserver.analyze.event;

import kr.yuns.dropthepitchserver.analyze.service.FileAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//새 작업이 저장된 뒤 분석을 시작한다.
//AFTER_COMMIT인 이유: 커밋 전에 실행하면 다른 스레드에서 Analysis 행을 아직 못 찾는다.
//@Async인 이유: 분석이 수십 초 걸리므로 업로드 응답을 붙잡아두지 않는다.
@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectCreatedEventListener {

    private final FileAnalysisService fileAnalysisService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectCreated(ProjectCreatedEvent event) {
        log.info("[onProjectCreated] 분석 트리거: projectId={}", event.projectId());
        fileAnalysisService.analyze(event.projectId());
    }
}
