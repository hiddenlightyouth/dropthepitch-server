package kr.yuns.dropthepitchserver.report.data.dto.ai;

//리포트 생성 호출 결과.
public record ReportGenerationCallResult(
        ReportGenerationResult result,
        String model,
        int inputTokens,
        int outputTokens
) { }
