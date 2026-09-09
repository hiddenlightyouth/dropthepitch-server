package kr.yuns.dropthepitchserver.report.service.ai;

import kr.yuns.dropthepitchserver.report.data.dto.ai.ReportGenerationCallResult;
import kr.yuns.dropthepitchserver.report.data.dto.ai.ReportGenerationResult;
import kr.yuns.dropthepitchserver.report.data.exception.ReportGenerationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;


@Component
@Slf4j
public class ReportGenerationClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ReportPromptLoader promptLoader;

    public ReportGenerationClient(ChatClient.Builder chatClientBuilder,
                                  ObjectMapper objectMapper,
                                  ReportPromptLoader promptLoader) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.promptLoader = promptLoader;
    }

    /**
     * 분석 결과와 수집된 의견으로 리포트 내용을 채움.
     * @param analysis 분석 브리프
     * @param opinions 페르소나별 의견을 정리한 글
     * @return 리포트 내용과 토큰 사용량
     */
    public ReportGenerationCallResult generate(String analysis, String opinions) {
        try {

            var options = GoogleGenAiChatOptions.builder()
                    .responseMimeType("application/json")
                    .responseSchema(promptLoader.schema());

            //토큰 사용량까지 필요하므로 ChatResponse로
            ChatResponse chatResponse = chatClient.prompt()
                    .options(options)
                    .system(promptLoader.systemPrompt())
                    .user(promptLoader.userPrompt(analysis, opinions))
                    .call()
                    .chatResponse();

            if (chatResponse == null || chatResponse.getResult() == null) {
                log.error("[generate] Gemini 응답이 비어 있습니다.");
                throw new ReportGenerationFailedException();
            }

            String json = chatResponse.getResult().getOutput().getText();
            ReportGenerationResult result = objectMapper.readValue(json, ReportGenerationResult.class);

            Usage usage = chatResponse.getMetadata().getUsage();
            return new ReportGenerationCallResult(
                    result,
                    chatResponse.getMetadata().getModel(),
                    tokenCount(usage == null ? null : usage.getPromptTokens()),
                    tokenCount(usage == null ? null : usage.getCompletionTokens()));

        } catch (ReportGenerationFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[generate] Gemini 호출 실패: cause={}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new ReportGenerationFailedException();
        }
    }
    private int tokenCount(Integer value) {
        if (value == null) {
            log.warn("[generate] 토큰 사용량이 응답에 없어 0으로 기록합니다.");
            return 0;
        }
        return value;
    }
}
