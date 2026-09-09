package kr.yuns.dropthepitchserver.opinion.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OpinionPromptLoader {

    private final String systemPromptTemplate;
    private final String schema;

    public OpinionPromptLoader() {
        this.systemPromptTemplate = read("prompts/opinion-system.txt");
        this.schema = read("schema/opinion-schema.json");
        log.info("[OpinionPromptLoader] 로딩 완료: system={}자, schema={}자",
                systemPromptTemplate.length(), schema.length());
    }

    public String schema() {
        return schema;
    }

    public String systemPrompt(String personaProfile) {
        return systemPromptTemplate.replace("{{PERSONA}}", personaProfile);
    }

    private String read(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 파일을 읽지 못했습니다: " + path, e);
        }
    }
}
