package kr.yuns.dropthepitchserver.analyze.service.ai;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.FileAnalysisResult;
import kr.yuns.dropthepitchserver.analyze.data.exception.FileAnalysisFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

//Gemini를 호출하는 유일한 창구. 모델을 바꾸거나 SDK를 직접 쓰게 되어도 이 클래스만 수정하면 된다.
//재시도는 Spring AI가 담당하므로(spring.ai.retry.* 설정) 여기서 따로 구현하지 않는다.
//스키마는 JSON 문자열로 직접 넘긴다. 자바 타입에서 생성하면 같은 중첩 타입을 여러 번 쓸 때
//$ref가 만들어지는데 Gemini가 이를 읽지 못해 값이 문자열로 돌아온다.
@Component
@Slf4j
public class GeminiAnalysisClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    //ChatClient는 재사용 가능하므로 생성자에서 한 번만 만든다.
    //ObjectMapper는 시큐리티 쪽 코드와 동일하게 스프링이 관리하는 것을 주입받는다.
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
            //options()는 빌드된 객체가 아니라 빌더를 받는다.
            var options = GoogleGenAiChatOptions.builder()
                    .responseMimeType("application/json")
                    .responseSchema(schema);

            //토큰 사용량까지 필요하므로 문자열이 아니라 ChatResponse로 받는다.
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
                throw new FileAnalysisFailedException();
            }

            String json = chatResponse.getResult().getOutput().getText();
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
            //Spring AI가 원본 예외를 RuntimeException으로 감싸므로 실제 원인은 cause에 들어있다.
            log.error("[analyze] Gemini 호출 실패: cause={}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new FileAnalysisFailedException();
        }
    }

    //Usage의 토큰 수는 Integer라 null일 수 있다. 사용량 기록 때문에 분석 전체를 실패시키지는 않는다.
    private int tokenCount(Integer value) {
        if (value == null) {
            log.warn("[analyze] 토큰 사용량이 응답에 없어 0으로 기록합니다.");
            return 0;
        }
        return value;
    }
}
