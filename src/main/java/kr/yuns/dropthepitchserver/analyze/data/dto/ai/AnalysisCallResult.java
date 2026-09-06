package kr.yuns.dropthepitchserver.analyze.data.dto.ai;

//Gemini 호출 결과. 분석 내용과 함께 ai_usage 기록에 필요한 토큰 사용량을 담는다.
//rawJson은 모델이 돌려준 원본 문자열이다. 객체로 다시 만들지 않고 이걸 그대로 저장하면
//스키마에 필드를 추가해도 자바 코드를 고칠 필요가 없다.
public record AnalysisCallResult(
        FileAnalysisResult result,
        String rawJson,
        String model,
        int inputTokens,
        int outputTokens
) { }
