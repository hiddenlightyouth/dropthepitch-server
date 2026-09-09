package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.analyze.service.thumbnail.PdfThumbnailExtractor;
import kr.yuns.dropthepitchserver.analyze.service.thumbnail.ThumbnailImageWriter;
import kr.yuns.dropthepitchserver.analyze.service.thumbnail.VideoThumbnailExtractor;
import kr.yuns.dropthepitchserver.common.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

//업로드된 파일의 썸네일을 만들어 file.thumbnailUrl에 남긴다.
//파일 내용은 UploadedFileProcessor가 한 번 내려받아 넘겨준다.
//분석과 별개라 여기서 실패해도 분석 결과에는 영향이 없다.
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbnailService {

    private static final String KEY_PREFIX = "thumbnails/";

    private final S3Service s3Service;
    private final ThumbnailResultService thumbnailResultService;
    private final PdfThumbnailExtractor pdfThumbnailExtractor;
    private final VideoThumbnailExtractor videoThumbnailExtractor;

    /**
     * 파일 내용에서 썸네일을 만들어 저장합니다.
     * 어떤 이유로 실패하더라도 예외를 밖으로 던지지 않습니다. 썸네일이 없으면 화면은 아이콘을 보여줍니다.
     *
     * @param projectId 프로젝트 ID
     * @param file 업로드된 파일
     * @param bytes 파일 내용
     */
    public void generate(Long projectId, File file, byte[] bytes) {
        long start = System.currentTimeMillis();
        InputType type = file.getType();

        try {
            //이미지는 원본이 곧 썸네일이다. 새로 만들지 않고 같은 key를 가리키게 한다.
            if (isImage(type)) {
                thumbnailResultService.saveKey(projectId, file.getUrl());
                log.info("[generate] 원본 이미지 사용: projectId={}, type={}", projectId, type);
                return;
            }

            byte[] thumbnail = extract(type, bytes);

            if (thumbnail == null) {
                log.info("[generate] 썸네일을 만들지 않았습니다: projectId={}, type={}", projectId, type);
                return;
            }

            String key = KEY_PREFIX + UUID.randomUUID() + "." + ThumbnailImageWriter.EXTENSION;
            s3Service.upload(thumbnail, key, ThumbnailImageWriter.CONTENT_TYPE);
            thumbnailResultService.saveKey(projectId, key);

            log.info("[generate] 썸네일 생성 완료: projectId={}, type={}, {}KB, {}ms",
                    projectId, type, thumbnail.length / 1024, System.currentTimeMillis() - start);

        } catch (Exception | LinkageError e) {
            //썸네일은 화면 장식이라 실패해도 분석과 리포트는 그대로 진행되어야 한다.
            //LinkageError까지 잡는 이유: ffmpeg 네이티브 로딩이 실패하면 UnsatisfiedLinkError가 나는데
            //이건 Exception이 아니라서 그냥 두면 밖으로 빠져나가 분석까지 멈춘다.
            //OutOfMemoryError는 VirtualMachineError라 여기서 안 잡히고 그대로 올라간다.
            log.error("[generate] 썸네일 생성 실패: projectId={}, type={}", projectId, type, e);
        }
    }

    //MD는 그림으로 만들 시각 요소가 없어 썸네일을 만들지 않는다.
    private byte[] extract(InputType type, byte[] bytes) throws Exception {
        return switch (type) {
            case PDF -> pdfThumbnailExtractor.extract(bytes);
            case MP4 -> videoThumbnailExtractor.extract(bytes);
            default -> null;
        };
    }

    private boolean isImage(InputType type) {
        return type == InputType.JPG || type == InputType.PNG || type == InputType.WEBP;
    }
}
