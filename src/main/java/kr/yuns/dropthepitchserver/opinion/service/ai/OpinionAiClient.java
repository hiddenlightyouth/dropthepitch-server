package kr.yuns.dropthepitchserver.opinion.service.ai;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.ai.service.AiService;
import kr.yuns.dropthepitchserver.ai.service.dto.SaveAiUsageCommand;
import kr.yuns.dropthepitchserver.opinion.service.ai.dto.OpinionAiResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpinionAiClient {
    private final OpinionPromptLoader opinionPromptLoader;
    private final GoogleGenAiChatModel chatModel;
    private final PersonaProfileProvider personaProfileProvider;
    private final AiService aiService;

    private final BeanOutputConverter<OpinionAiResultDto> converter =
            new BeanOutputConverter<>(OpinionAiResultDto.class);

    /**
     * 페르소나 의견 수집을 위해 AI 모델을 호출합니다.
     *
     * @param projectId 프로젝트 ID
     * @param personaId Persona
     * @param userMsg 요청 MSG
     * @return OpinionAiResultDto
     */
    public OpinionAiResultDto callAiModel(Long projectId, Long personaId, String userMsg) {
        String systemPrompt = String.join(System.lineSeparator(),
                opinionPromptLoader.systemPrompt(),
                personaProfileProvider.getProfile(personaId),
                converter.getFormat());

        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMsg)
        );

        Prompt prompt = Prompt.builder()
                .messages(messages)
                .build();

        ChatResponse response = chatModel.call(prompt);
        saveAiUsage(projectId, response);

        String content = response.getResult().getOutput().getText();
        log.debug("[callAiModel] 페르소나 {} 의견 수집: {}", personaId, content);

        return converter.convert(content);
    }

    /**
     * AI 호출 로그를 저장합니다.
     *
     * @param projectId 프로젝트 ID
     * @param response AI 응답
     */
    private void saveAiUsage(Long projectId, ChatResponse response) {
        ChatResponseMetadata metadata = response.getMetadata();
        Usage usage = metadata.getUsage();

        try {
            aiService.saveAiUsage(new SaveAiUsageCommand(
                    projectId,
                    metadata.getModel(),
                    AiPurpose.OPINION,
                    usage.getPromptTokens(),
                    usage.getCompletionTokens()
            ));
        } catch (Exception e) {
            log.warn("[saveAiUsage] AI 사용량 저장 실패, 의견 수집은 계속합니다: projectId={}", projectId, e);
        }
    }
}
