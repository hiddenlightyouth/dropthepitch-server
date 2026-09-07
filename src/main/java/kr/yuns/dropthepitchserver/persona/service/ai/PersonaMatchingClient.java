package kr.yuns.dropthepitchserver.persona.service.ai;

import kr.yuns.dropthepitchserver.persona.data.dto.ai.TagSelectionCallResult;
import kr.yuns.dropthepitchserver.persona.data.dto.ai.TagSelectionResult;
import kr.yuns.dropthepitchserver.persona.data.exception.TagSelectionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@Slf4j
public class PersonaMatchingClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final TagSelectionPromptLoader promptLoader;

    public PersonaMatchingClient(ChatClient.Builder chatClientBuilder,
                                 ObjectMapper objectMapper,
                                 TagSelectionPromptLoader promptLoader) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.promptLoader = promptLoader;
    }

    /**
     * @param allTags DB에 실재하는 태그 이름 목록.
     * @param analysis 분석 브리프
     * @return 고른 태그와 토큰 사용량
     */
    public TagSelectionCallResult selectTags(List<String> allTags, String analysis) {
        try {
            //options()는 빌드된 객체가 아니라 빌더를 받는다.
            var options = GoogleGenAiChatOptions.builder()
                    .responseMimeType("application/json")
                    .responseSchema(promptLoader.schema());

            ChatResponse chatResponse = chatClient.prompt()
                    .options(options)
                    .system(promptLoader.systemPrompt())
                    .user(promptLoader.userPrompt(allTags, analysis))
                    .call()
                    .chatResponse();

            if (chatResponse == null || chatResponse.getResult() == null) {
                log.error("[selectTags] Gemini 응답이 비어 있습니다.");
                throw new TagSelectionFailedException();
            }

            String json = chatResponse.getResult().getOutput().getText();
            TagSelectionResult result = objectMapper.readValue(json, TagSelectionResult.class);

            Usage usage = chatResponse.getMetadata().getUsage();
            return new TagSelectionCallResult(
                    result,
                    chatResponse.getMetadata().getModel(),
                    tokenCount(usage == null ? null : usage.getPromptTokens()),
                    tokenCount(usage == null ? null : usage.getCompletionTokens()));

        } catch (TagSelectionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[selectTags] Gemini 호출 실패: cause={}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new TagSelectionFailedException();
        }
    }

    //Usage의 토큰 수는 Integer라 null일 수 있다. 사용량 기록 때문에 선별 전체를 실패시키지는 않는다.
    private int tokenCount(Integer value) {
        if (value == null) {
            log.warn("[selectTags] 토큰 사용량이 응답에 없어 0으로 기록합니다.");
            return 0;
        }
        return value;
    }
}
