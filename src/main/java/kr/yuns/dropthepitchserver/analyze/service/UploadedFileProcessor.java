package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.common.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadedFileProcessor {

    private static final int MAX_BYTES = 100 * 1024 * 1024;

    private final S3Service s3Service;
    private final AnalysisResultService analysisResultService;
    private final ThumbnailService thumbnailService;
    private final FileAnalysisService fileAnalysisService;

    /**
     * 업로드된 파일의 썸네일을 만들고 분석을 실행합니다.
     * 어떤 이유로 실패하더라도 예외를 밖으로 던지지 않고 분석 상태를 FAILED로 남깁니다.
     *
     * @param projectId 프로젝트 ID
     */
    public void process(Long projectId) {
        try {
            File file = analysisResultService.getFile(projectId);

            if (isTooLarge(file)) {
                log.error("[process] 인라인 전송 한도 초과: projectId={}, type={}, size={}bytes",
                        projectId, file.getType(), file.getSize());
                analysisResultService.saveFailure(projectId, AnalysisVerdict.AI_ERROR);
                return;
            }

            long start = System.currentTimeMillis();
            byte[] bytes = s3Service.download(file.getUrl());
            log.info("[process] 파일 준비 완료: projectId={}, {}ms", projectId, System.currentTimeMillis() - start);

            thumbnailService.generate(projectId, file, bytes);
            fileAnalysisService.analyze(projectId, file, bytes);

        } catch (Exception e) {
            log.error("[process] 파일 처리 실패: projectId={}", projectId, e);
            saveFailureQuietly(projectId);
        }
    }

    private boolean isTooLarge(File file) {
        return file.getSize() > MAX_BYTES;
    }

    private void saveFailureQuietly(Long projectId) {
        try {
            analysisResultService.saveFailure(projectId, AnalysisVerdict.AI_ERROR);
        } catch (Exception e) {
            log.error("[process] 실패 상태 기록마저 실패: projectId={}", projectId, e);
        }
    }
}
