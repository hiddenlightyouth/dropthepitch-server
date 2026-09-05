package kr.yuns.dropthepitchserver.analyze.data.dto.ai;

//Gemini 호출 결과. 분석 내용과 함께 ai_usage 기록에 필요한 토큰 사용량을 담는다.
public record AnalysisCallResult(
        FileAnalysisResult result,
        String model,
        int inputTokens,
        int outputTokens
) { }
