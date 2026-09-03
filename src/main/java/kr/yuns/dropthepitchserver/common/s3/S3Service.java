package kr.yuns.dropthepitchserver.common.s3;

import kr.yuns.dropthepitchserver.common.s3.exception.FileUploadFailedException;
import kr.yuns.dropthepitchserver.common.s3.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {
    private static final String KEY_PREFIX = "projects/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png", "webp", "mp4", "pdf", "md");
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * 파일을 검증하고 S3에 업로드한 뒤 저장된 key를 반환한다.
     * @param file 업로드할 파일
     * @return S3에 저장된 파일의 key
     */
    public String upload(MultipartFile file) {
        validateFile(file);

        String key = KEY_PREFIX + UUID.randomUUID() + "." + extension(file.getOriginalFilename());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException e) {
            log.error("[S3Service] S3 업로드 실패: key={}, message={}", key, e.getMessage());
            throw new FileUploadFailedException();
        }

        log.info("[S3Service] S3 업로드 완료: key={}", key);
        return key;
    }

    /**
     * 업로드할 파일이 비어있지 않고 허용된 확장자인지 검증합니다.
     *
     * @param file 업로드할 파일
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("[S3Service] 업로드할 파일이 비어있습니다.");
            throw new InvalidFileTypeException();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension(file.getOriginalFilename()))) {
            log.error("[S3Service] 허용되지 않은 확장자: {}", file.getOriginalFilename());
            throw new InvalidFileTypeException();
        }
    }

    /**
     * 파일명에서 확장자를 소문자로 추출합니다.
     *
     * @param originalFilename 원본 파일명
     * @return 소문자 확장자, 없으면 빈 문자열
     */
    private String extension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot + 1).toLowerCase() : "";
    }
}