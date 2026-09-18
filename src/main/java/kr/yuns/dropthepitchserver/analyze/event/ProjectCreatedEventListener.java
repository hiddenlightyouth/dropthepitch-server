package kr.yuns.dropthepitchserver.analyze.event;

import kr.yuns.dropthepitchserver.analyze.service.UploadedFileProcessor;
import kr.yuns.dropthepitchserver.common.async.AsyncConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//새 작업이 저장된 뒤 썸네일 생성과 분석을 시작한다.
//AFTER_COMMIT인 이유: 커밋 전에 실행하면 다른 스레드에서 File 행을 아직 못 찾는다.
//@Async인 이유: 분석이 수십 초 걸리므로 업로드 응답을 붙잡아두지 않는다.
@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectCreatedEventListener {

    private final UploadedFileProcessor uploadedFileProcessor;

    @Async(AsyncConfiguration.ANALYSIS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectCreated(ProjectCreatedEvent event) {
        log.info("[onProjectCreated] 파일 처리 트리거: projectId={}", event.projectId());
        uploadedFileProcessor.process(event.projectId());
    }
}
