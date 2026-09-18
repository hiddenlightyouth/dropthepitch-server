package kr.yuns.dropthepitchserver.project.event;

import java.util.List;

//커밋 뒤에는 File 행이 없으므로 지울 S3 key를 미리 담아 보낸다.
public record ProjectDeletedEvent(Long projectId, List<String> fileKeys) { }
