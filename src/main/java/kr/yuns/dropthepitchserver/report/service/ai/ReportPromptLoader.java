package kr.yuns.dropthepitchserver.report.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class ReportPromptLoader {

    private final String systemPrompt;
    private final String userPromptTemplate;
    private final String schema;

    public ReportPromptLoader() {
        this.systemPrompt = read("prompts/report-system.txt");
        this.userPromptTemplate = read("prompts/report-user.txt");
        this.schema = read("schema/report-schema.json");

        log.info("[ReportPromptLoader] 로딩 완료: system={}자, schema={}자",
                systemPrompt.length(), schema.length());
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    public String schema() {
        return schema;
    }

    public String userPrompt(String analysis, String opinions) {
        return userPromptTemplate
                .replace("{{ANALYSIS}}", analysis)
                .replace("{{OPINIONS}}", opinions);
    }

    private String read(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 파일을 읽지 못했습니다: " + path, e);
        }
    }
}
