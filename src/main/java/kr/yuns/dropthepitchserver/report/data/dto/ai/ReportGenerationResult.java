package kr.yuns.dropthepitchserver.report.data.dto.ai;

import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;

import java.util.List;


public record ReportGenerationResult(
        String summary,
        String insight,
        List<String> positivePoints,
        List<String> negativePoints,
        List<AgeInsight> ageInsights
) {
    public record AgeInsight(
            AgeGroup ageGroup,
            String keyPoint,
            String painPoint,
            String improvement
    ) { }
}
