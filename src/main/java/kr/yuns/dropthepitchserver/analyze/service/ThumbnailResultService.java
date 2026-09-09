package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.exception.FileNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//썸네일 쪽 DB 작업만 담당한다.
//AnalysisResultService와 같은 이유로 분리했다. 그림을 만드는 동안 트랜잭션을 열어두지 않기 위해서다.
@Service
@RequiredArgsConstructor
public class ThumbnailResultService {

    private final FileRepository fileRepository;

    /**
     * 썸네일 S3 key를 저장합니다.
     * 조회 시점에 presigned URL로 바뀌므로 완성된 주소가 아니라 key를 넣습니다.
     *
     * @param projectId 프로젝트 ID
     * @param key S3에 저장된 썸네일의 key
     */
    @Transactional
    public void saveKey(Long projectId, String key) {
        fileRepository.findByProjectId(projectId)
                .orElseThrow(FileNotFoundException::new)
                .updateThumbnailUrl(key);
    }
}
