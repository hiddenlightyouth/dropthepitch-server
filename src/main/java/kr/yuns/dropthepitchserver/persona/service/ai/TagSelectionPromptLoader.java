package kr.yuns.dropthepitchserver.persona.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

//태그 선별 프롬프트와 JSON 스키마를 resources에서 읽어 보관.
@Component
@Slf4j
public class TagSelectionPromptLoader {

    private final String systemPrompt;
    private final String userPromptTemplate;
    private final String schema;

    public TagSelectionPromptLoader() {
        this.systemPrompt = read("prompts/tag-selection-system.txt");
        this.userPromptTemplate = read("prompts/tag-selection-user.txt");
        this.schema = read("schema/tag-selection-schema.json");
        log.info("[TagSelectionPromptLoader] 로딩 완료: system={}자, schema={}자",
                systemPrompt.length(), schema.length());
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    public String schema() {
        return schema;
    }


     //태그 목록과 분석 브리프를 사용자 프롬프트에 끼워넣음
     /** 각 파라미터가 의미하는것
      * tags : DB에 실재하는 태그 이름 목록
      * analysis : 분석 브리프
     */
    public String userPrompt(List<String> tags, String analysis) {
        return userPromptTemplate
                .replace("{{TAGS}}", String.join("\n", tags))
                .replace("{{ANALYSIS}}", analysis);
    }
    
    private String read(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 파일을 읽지 못했습니다: " + path, e);
        }
    }
}
