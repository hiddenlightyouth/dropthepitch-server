package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.common.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//업로드된 파일 하나로 썸네일과 분석을 잇따라 처리한다.
//S3에서 한 번만 내려받아 둘이 나눠 쓴다. 예전에는 각자 받아서 같은 파일이 두 번 오갔고,
//한 회선을 나눠 쓰느라 44MB짜리 하나에 3분이 걸렸다.
//@Transactional을 붙이지 않는다. 내려받기와 AI 호출에 시간이 걸리는데 그동안 DB 커넥션을 잡으면
//커넥션 풀이 마른다. DB 작업은 각 ResultService의 짧은 트랜잭션에 맡긴다.
@Service
@Slf4j
@RequiredArgsConstructor
public class UploadedFileProcessor {

    //Gemini는 전체 요청 크기가 100MB(PDF는 50MB)를 넘으면 인라인 전송을 받지 않는다.
    //프롬프트와 인코딩 여유를 두고 한도보다 낮게 잡는다. 넘는 파일은 Files API가 필요하다.
    private static final int MAX_BYTES = 80 * 1024 * 1024;
    private static final int MAX_BYTES_PDF = 40 * 1024 * 1024;

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

            //내려받기 전에 크기부터 본다. 업로드는 1GB까지 허용하므로 먼저 걸러야 메모리가 터지지 않는다.
            if (isTooLarge(file)) {
                log.error("[process] 인라인 전송 한도 초과: projectId={}, type={}, size={}bytes",
                        projectId, file.getType(), file.getSize());
                analysisResultService.saveFailure(projectId);
                return;
            }

            long start = System.currentTimeMillis();
            byte[] bytes = s3Service.download(file.getUrl());
            log.info("[process] 파일 준비 완료: projectId={}, {}ms", projectId, System.currentTimeMillis() - start);

            //썸네일이 1초 안쪽이라 먼저 만든다. 수십 초 걸리는 분석을 기다리지 않고 화면에 그림이 뜬다.
            thumbnailService.generate(projectId, file, bytes);
            fileAnalysisService.analyze(projectId, file, bytes);

        } catch (Exception e) {
            log.error("[process] 파일 처리 실패: projectId={}", projectId, e);
            saveFailureQuietly(projectId);
        }
    }

    private boolean isTooLarge(File file) {
        return file.getSize() > (file.getType() == InputType.PDF ? MAX_BYTES_PDF : MAX_BYTES);
    }

    //실패를 기록하다가 또 실패하면 원래 오류가 묻힌다. 여기서 끊는다.
    private void saveFailureQuietly(Long projectId) {
        try {
            analysisResultService.saveFailure(projectId);
        } catch (Exception e) {
            log.error("[process] 실패 상태 기록마저 실패: projectId={}", projectId, e);
        }
    }
}
