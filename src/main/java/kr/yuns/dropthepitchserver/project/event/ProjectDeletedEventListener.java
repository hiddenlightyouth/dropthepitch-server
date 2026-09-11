package kr.yuns.dropthepitchserver.project.event;

import kr.yuns.dropthepitchserver.common.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//DB 삭제가 확정된 뒤에 S3 파일을 지운다. 먼저 지우면 롤백됐을 때 파일만 사라진다.
@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectDeletedEventListener {

    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectDeleted(ProjectDeletedEvent event) {
        log.info("[onProjectDeleted] S3 파일 삭제: projectId={}, 파일 {}개", event.projectId(), event.fileKeys().size());
        event.fileKeys().forEach(s3Service::delete);
    }
}
