package kr.yuns.dropthepitchserver.analyze.service.ai;

import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//프롬프트와 JSON 스키마를 resources에서 읽어 보관한다.
//파일로 두는 이유: 프롬프트를 고칠 때 자바 코드를 건드리지 않아도 되고,
//AI Studio나 터미널 테스트에서 검증한 파일을 서버가 그대로 쓰기 때문이다.
@Component
@Slf4j
public class AnalysisPromptLoader {

    private final String systemPrompt;
    private final String documentPrompt;
    private final String imagePrompt;
    private final String videoPrompt;
    private final String schema;

    //파일은 바뀌지 않으므로 시작할 때 한 번만 읽는다.
    public AnalysisPromptLoader() {
        this.systemPrompt = read("prompts/analysis-system.txt");
        this.documentPrompt = read("prompts/analysis-user-document.txt");
        this.imagePrompt = read("prompts/analysis-user-image.txt");
        this.videoPrompt = read("prompts/analysis-user-video.txt");
        this.schema = read("schema/analysis-schema.json");

        log.info("[AnalysisPromptLoader] 로딩 완료: system={}자, schema={}자",
                systemPrompt.length(), schema.length());
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    public String schema() {
        return schema;
    }

    /**
     * 파일 형식에 맞는 사용자 프롬프트를 돌려줍니다.
     * 문서, 이미지, 영상은 읽는 방법이 달라 프롬프트가 나뉩니다.
     *
     * @param type 업로드된 파일 형식
     * @return 사용자 프롬프트
     */
    public String userPrompt(InputType type) {
        return switch (type) {
            case PDF, MD -> documentPrompt;
            case JPG, PNG, WEBP -> imagePrompt;
            case MP4 -> videoPrompt;
        };
    }

    /**
     * 파일 형식을 Gemini에 실어 보낼 MIME 타입으로 바꿉니다.
     * MD는 text/markdown이 아니라 text/plain으로 보내야 정상 처리됩니다.
     *
     * @param type 업로드된 파일 형식
     * @return MIME 타입
     */
    public MimeType mimeType(InputType type) {
        return switch (type) {
            case JPG -> MimeTypeUtils.IMAGE_JPEG;
            case PNG -> MimeTypeUtils.IMAGE_PNG;
            case WEBP -> MimeType.valueOf("image/webp");
            case MP4 -> MimeType.valueOf("video/mp4");
            case PDF -> MimeType.valueOf("application/pdf");
            case MD -> MimeTypeUtils.TEXT_PLAIN;
        };
    }

    //파일이 없거나 깨졌으면 서버가 아예 뜨지 않게 한다. 분석 요청이 들어온 뒤에 알면 늦다.
    private String read(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 파일을 읽지 못했습니다: " + path, e);
        }
    }
}
