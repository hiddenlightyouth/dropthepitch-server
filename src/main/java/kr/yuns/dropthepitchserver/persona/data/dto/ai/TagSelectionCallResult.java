package kr.yuns.dropthepitchserver.persona.data.dto.ai;

//고른 태그와 함께 ai_usage 기록에 필요한 토큰 사용량을 담음.
public record TagSelectionCallResult(
        TagSelectionResult result,
        String model,
        int inputTokens,
        int outputTokens
) { }
