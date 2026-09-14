package kr.yuns.dropthepitchserver.analyze.service.ai;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import com.google.genai.errors.ApiException;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.FileAnalysisResult;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.analyze.data.exception.FileAnalysisFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiSafetySetting;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

//스키마를 자바 타입에서 생성하면 $ref가 생겨 Gemini가 읽지 못하므로 JSON 문자열로 넘긴다.
@Component
@Slf4j
public class GeminiAnalysisClient {

    private static final List<GoogleGenAiSafetySetting> SAFETY_SETTINGS = List.of(
            safety(GoogleGenAiSafetySetting.HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT),
            safety(GoogleGenAiSafetySetting.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT));
    private static final List<String> BLOCKED_FINISH_REASONS = List.of("SAFETY", "PROHIBITED", "BLOCKLIST", "SPII");

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public GeminiAnalysisClient(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 프롬프트와 첨부 파일을 Gemini에 보내 분석 결과를 받습니다.
     *
     * @param systemPrompt 역할과 규칙
     * @param userPrompt 이번 요청의 지시
     * @param schema 응답 형식을 정하는 JSON 스키마 문자열
     * @param mediaList 이미지, PDF, 영상 첨부. 첨부가 없으면 빈 리스트
     * @return 분석 결과와 토큰 사용량
     */
    public AnalysisCallResult analyze(String systemPrompt, String userPrompt, String schema, List<Media> mediaList) {
        try {
            var options = GoogleGenAiChatOptions.builder()
                    .responseMimeType("application/json")
                    .responseSchema(schema)
                    .safetySettings(SAFETY_SETTINGS);

            ChatResponse chatResponse = chatClient.prompt()
                    .options(options)
                    .system(systemPrompt)
                    .user(user -> {
                        user.text(userPrompt);
                        if (!mediaList.isEmpty()) {
                            user.media(mediaList.toArray(Media[]::new));
                        }
                    })
                    .call()
                    .chatResponse();

            if (chatResponse == null || chatResponse.getResult() == null) {
                log.error("[analyze] Gemini 응답이 비어 있습니다.");
                throw new FileAnalysisFailedException(AnalysisVerdict.AI_ERROR);
            }

            String json = chatResponse.getResult().getOutput().getText();
            if (!StringUtils.hasText(json)) {
                String finishReason = String.valueOf(chatResponse.getResult().getMetadata().getFinishReason());
                log.warn("[analyze] Gemini가 내용을 돌려주지 않았습니다: finishReason={}", finishReason);
                throw new FileAnalysisFailedException(isBlocked(finishReason) ? AnalysisVerdict.ADULT : AnalysisVerdict.AI_ERROR);
            }
            FileAnalysisResult result = objectMapper.readValue(json, FileAnalysisResult.class);

            Usage usage = chatResponse.getMetadata().getUsage();
            return new AnalysisCallResult(
                    result,
                    json,
                    chatResponse.getMetadata().getModel(),
                    tokenCount(usage == null ? null : usage.getPromptTokens()),
                    tokenCount(usage == null ? null : usage.getCompletionTokens()));

        } catch (FileAnalysisFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[analyze] Gemini 호출 실패: cause={}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new FileAnalysisFailedException(isOverloaded(e) ? AnalysisVerdict.AI_BUSY : AnalysisVerdict.AI_ERROR);
        }
    }

    private int tokenCount(Integer value) {
        if (value == null) {
            log.warn("[analyze] 토큰 사용량이 응답에 없어 0으로 기록합니다.");
            return 0;
        }
        return value;
    }

    private static GoogleGenAiSafetySetting safety(GoogleGenAiSafetySetting.HarmCategory category) {
        return new GoogleGenAiSafetySetting.Builder()
                .withCategory(category)
                .withThreshold(GoogleGenAiSafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                .build();
    }

    private static boolean isBlocked(String finishReason) {
        return BLOCKED_FINISH_REASONS.stream().anyMatch(finishReason::contains);
    }

    private static boolean isOverloaded(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException api && (api.code() == 429 || api.code() == 503)) {
                return true;
            }
        }
        return false;
    }
}
