package kr.yuns.dropthepitchserver.opinion.service.ai;

import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionCollectionFailedException;
import kr.yuns.dropthepitchserver.opinion.service.ai.dto.OpinionAiResultDto;
import kr.yuns.dropthepitchserver.opinion.service.ai.dto.OpinionCallResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class OpinionAiClient {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final OpinionPromptLoader promptLoader;
    private final PersonaProfileProvider personaProfileProvider;

    public OpinionAiClient(ChatClient.Builder chatClientBuilder,
                           ObjectMapper objectMapper,
                           OpinionPromptLoader promptLoader,
                           PersonaProfileProvider personaProfileProvider) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.promptLoader = promptLoader;
        this.personaProfileProvider = personaProfileProvider;
    }

    public OpinionCallResult collectOpinion(Long personaId, String analysisBrief) {
        try {
            var options = GoogleGenAiChatOptions.builder()
                    .responseMimeType("application/json")
                    .responseSchema(promptLoader.schema());

            ChatResponse chatResponse = chatClient.prompt()
                    .options(options)
                    .system(promptLoader.systemPrompt(personaProfileProvider.getProfile(personaId)))
                    .user(analysisBrief)
                    .call()
                    .chatResponse();

            if (chatResponse == null || chatResponse.getResult() == null) {
                log.error("[collectOpinion] Gemini가 빈 응답을 반환함: personaId={}", personaId);
                throw new OpinionCollectionFailedException();
            }

            String json = chatResponse.getResult().getOutput().getText();
            log.debug("[collectOpinion] 페르소나 {} 의견 수집: {}", personaId, json);

            OpinionAiResultDto result = objectMapper.readValue(json, OpinionAiResultDto.class);

            Usage usage = chatResponse.getMetadata().getUsage();
            return new OpinionCallResult(
                    result,
                    chatResponse.getMetadata().getModel(),
                    tokenCount(usage.getPromptTokens()),
                    tokenCount(usage.getCompletionTokens()));

        } catch (OpinionCollectionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[collectOpinion] Gemini를 호출할 수 없음: personaId={}, cause={}", personaId,
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new OpinionCollectionFailedException();
        }
    }

    private int tokenCount(Integer value) {
        if (value == null) {
            log.warn("[collectOpinion] 토큰 사용량 정보를 불러올 수 없음");
            return 0;
        }
        return value;
    }
}
