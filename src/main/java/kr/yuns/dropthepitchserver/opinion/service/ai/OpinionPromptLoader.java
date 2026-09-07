package kr.yuns.dropthepitchserver.opinion.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OpinionPromptLoader {
    private static final String SYSTEM_PROMPT_PATH = "prompts/opinion-system.txt";

    private final String systemPrompt;

    public OpinionPromptLoader() {
        this.systemPrompt = read();

        log.info("[OpinionPromptLoader] 로딩 완료: system={}자", systemPrompt.length());
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    private String read() {
        try {
            return new ClassPathResource(SYSTEM_PROMPT_PATH).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("[OpinionPromptLoader] 프롬프트 파일 로드 실패: " + SYSTEM_PROMPT_PATH, e);
        }
    }
}
