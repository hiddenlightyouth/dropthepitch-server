package kr.yuns.dropthepitchserver.common.s3;

import kr.yuns.dropthepitchserver.common.s3.exception.FileDownloadFailedException;
import kr.yuns.dropthepitchserver.common.s3.exception.FileUploadFailedException;
import kr.yuns.dropthepitchserver.common.s3.exception.FileTooLargeException;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {
    private static final String KEY_PREFIX = "projects/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png", "webp", "mp4", "pdf", "md");
    //브라우저가 보낸 형식은 charset이 없어 한글 텍스트가 깨져 보인다. 확장자로 직접 정한다.
    private static final Set<String> TEXT_EXTENSIONS = Set.of("md");
    private static final long MAX_BYTES = 100L * 1024 * 1024;
    private static final int SIGNATURE_LENGTH = 12;
    private static final Charset CP949 = Charset.forName("x-windows-949");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "md", "text/markdown; charset=UTF-8",
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "mp4", "video/mp4");
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
                            .contentType(contentType(file.getOriginalFilename()))
                            .build(),
                    body(file));
        } catch (IOException | SdkException e) {
            log.error("[S3Service] S3 업로드 실패: key={}, message={}", key, e.getMessage());
            throw new FileUploadFailedException();
        }

        log.info("[S3Service] S3 업로드 완료: key={}", key);
        return key;
    }

    /**
     * 서버가 만들어낸 파일을 S3에 올립니다.
     * 업로드와 달리 확장자 검증을 하지 않습니다. 사용자가 준 파일이 아니라 우리가 만든 것이기 때문입니다.
     *
     * @param bytes 올릴 내용
     * @param key S3에 저장할 key
     * @param contentType 내용 형식
     */
    public void upload(byte[] bytes, String key, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(bytes));
        } catch (SdkException e) {
            log.error("[S3Service] S3 업로드 실패: key={}, message={}", key, e.getMessage());
            throw new FileUploadFailedException();
        }

        log.info("[S3Service] S3 업로드 완료: key={}, size={}bytes", key, bytes.length);
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
     * S3에 저장된 파일을 지웁니다.
     * 실패해도 예외를 던지지 않습니다. DB에서 이미 지운 뒤라 되돌릴 수 없고, 남은 파일은 저장 공간만 차지합니다.
     *
     * @param key S3에 저장된 파일의 key
     */
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("[S3Service] S3 삭제 완료: key={}", key);
        } catch (SdkException e) {
            log.warn("[S3Service] S3 삭제 실패: key={}, message={}", key, e.getMessage());
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
                                .responseContentType(contentType(key))
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

        if (file.getSize() > MAX_BYTES) {
            log.error("[S3Service] 허용 크기 초과: {}bytes", file.getSize());
            throw new FileTooLargeException();
        }

        if (!matchesSignature(extension(file.getOriginalFilename()), head(file))) {
            log.error("[S3Service] 확장자와 실제 내용이 다릅니다: {}", file.getOriginalFilename());
            throw new InvalidFileTypeException();
        }
    }

    private byte[] head(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(SIGNATURE_LENGTH);
        } catch (IOException e) {
            log.error("[S3Service] 파일을 읽지 못했습니다: {}", file.getOriginalFilename());
            throw new InvalidFileTypeException();
        }
    }

    //확장자는 바꿔 달 수 있어 파일 앞부분으로 실제 형식을 확인한다.
    private static boolean matchesSignature(String extension, byte[] head) {
        return switch (extension) {
            case "pdf" -> ascii(head, 0, 4).equals("%PDF");
            case "png" -> head.length >= 4 && (head[0] & 0xFF) == 0x89 && ascii(head, 1, 3).equals("PNG");
            case "jpg" -> head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8;
            case "webp" -> ascii(head, 0, 4).equals("RIFF") && ascii(head, 8, 4).equals("WEBP");
            case "mp4" -> ascii(head, 4, 4).equals("ftyp");
            case "md" -> isText(head);
            default -> false;
        };
    }

    private static String ascii(byte[] head, int offset, int length) {
        return head.length < offset + length ? "" : new String(head, offset, length, StandardCharsets.US_ASCII);
    }

    //텍스트에는 널 바이트가 없다. 실행 파일이나 이미지를 md로 바꿔 올리면 여기서 걸린다.
    private static boolean isText(byte[] head) {
        for (byte b : head) {
            if (b == 0) {
                return false;
            }
        }
        return true;
    }

    //윈도우에서 만든 텍스트는 CP949로 저장돼 온다. 그대로 두면 화면에서도 깨지고 분석도 깨진 글자로 한다.
    private RequestBody body(MultipartFile file) throws IOException {
        if (!TEXT_EXTENSIONS.contains(extension(file.getOriginalFilename()))) {
            return RequestBody.fromInputStream(file.getInputStream(), file.getSize());
        }
        return RequestBody.fromBytes(toUtf8(file.getBytes()));
    }

    private static byte[] toUtf8(byte[] bytes) {
        try {
            StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes));
            return bytes;
        } catch (CharacterCodingException e) {
            return new String(bytes, CP949).getBytes(StandardCharsets.UTF_8);
        }
    }

    //이미 올라간 파일도 내려받을 때 이 형식으로 열리도록 서명 주소에 함께 넣는다.
    private String contentType(String name) {
        return CONTENT_TYPES.get(extension(name));
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