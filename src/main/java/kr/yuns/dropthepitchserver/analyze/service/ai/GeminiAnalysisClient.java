package kr.yuns.dropthepitchserver.analyze.service.ai;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.FileAnalysisResult;
import kr.yuns.dropthepitchserver.analyze.data.exception.FileAnalysisFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Component;

import java.util.List;

//Gemini를 호출하는 유일한 창구. 모델을 바꾸거나 SDK를 직접 쓰게 되어도 이 클래스만 수정하면 된다.
//재시도는 Spring AI가 담당하므로(spring.ai.retry.* 설정) 여기서 따로 구현하지 않는다.
@Component
@Slf4j
public class GeminiAnalysisClient {

    private final ChatClient chatClient;

    //ChatClient는 재사용 가능하므로 생성자에서 한 번만 만든다.
    public GeminiAnalysisClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 프롬프트와 첨부 파일을 Gemini에 보내 분석 결과를 받는다.
     *
     * @param systemPrompt 역할과 규칙
     * @param userPrompt 이번 요청의 지시와 자료
     * @param mediaList 이미지, PDF, 영상 첨부. 문서 텍스트만 보낼 때는 빈 리스트
     * @return 분석 결과와 토큰 사용량
     */
    public AnalysisCallResult analyze(String systemPrompt, String userPrompt, List<Media> mediaList) {
        try {
            ResponseEntity<ChatResponse, FileAnalysisResult> response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(user -> {
                        user.text(userPrompt);
                        if (!mediaList.isEmpty()) {
                            user.media(mediaList.toArray(Media[]::new));
                        }
                    })
                    .call()
                    .responseEntity(FileAnalysisResult.class, spec -> spec.useProviderStructuredOutput());

            FileAnalysisResult result = response.entity();
            if (result == null) {
                log.error("[analyze] Gemini 응답을 객체로 변환하지 못했습니다.");
                throw new FileAnalysisFailedException();
            }

            ChatResponse chatResponse = response.response();
            return new AnalysisCallResult(
                    result,
                    chatResponse.getMetadata().getModel(),
                    chatResponse.getMetadata().getUsage().getPromptTokens(),
                    chatResponse.getMetadata().getUsage().getCompletionTokens());

        } catch (FileAnalysisFailedException e) {
            throw e;
        } catch (Exception e) {
            //Spring AI가 원본 예외를 RuntimeException으로 감싸므로 실제 원인은 cause에 들어있다.
            log.error("[analyze] Gemini 호출 실패: cause={}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new FileAnalysisFailedException();
        }
    }
}
