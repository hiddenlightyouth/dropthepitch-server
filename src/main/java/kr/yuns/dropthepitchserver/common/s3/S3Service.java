package kr.yuns.dropthepitchserver.common.s3;

import kr.yuns.dropthepitchserver.common.s3.exception.FileDownloadFailedException;
import kr.yuns.dropthepitchserver.common.s3.exception.FileUploadFailedException;
import kr.yuns.dropthepitchserver.common.s3.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {
    private static final String KEY_PREFIX = "projects/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png", "webp", "mp4", "pdf", "md");
    //화면을 열어두고 리포트를 다 본 뒤에 눌러도 되도록 넉넉히 잡는다.
    private static final Duration DOWNLOAD_URL_DURATION = Duration.ofMinutes(30);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

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
     * S3에 저장된 파일을 바이트 배열로 내려받습니다.
     * AI에 인라인으로 실어 보낼 파일만 사용하므로 메모리에 한 번에 올립니다.
     *
     * @param key S3에 저장된 파일의 key
     * @return 파일 내용
     */
    public byte[] download(String key) {
        try {
            ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build());

            byte[] bytes = object.asByteArray();
            log.info("[S3Service] S3 다운로드 완료: key={}, size={}bytes", key, bytes.length);
            return bytes;
        } catch (SdkException e) {
            log.error("[S3Service] S3 다운로드 실패: key={}, message={}", key, e.getMessage());
            throw new FileDownloadFailedException();
        }
    }

    /**
     * S3 key를 브라우저가 열 수 있는 임시 주소로 바꿉니다.
     * 버킷이 비공개라 key만으로는 접근할 수 없고, 서명된 주소가 30분 동안만 열립니다.
     * 만료가 있으므로 저장하지 말고 조회할 때마다 새로 만들어야 합니다.
     *
     * @param key S3에 저장된 파일의 key. null이면 null을 돌려줍니다
     * @return 서명된 임시 주소
     */
    public String getDownloadUrl(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(DOWNLOAD_URL_DURATION)
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build())
                        .build())
                .url()
                .toExternalForm();
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