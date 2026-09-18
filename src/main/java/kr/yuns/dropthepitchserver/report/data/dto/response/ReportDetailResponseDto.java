package kr.yuns.dropthepitchserver.report.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportStatus;
import lombok.Builder;

import java.util.List;

//상세 리포트 Dto
@Builder
public record ReportDetailResponseDto(
        String reportId,
        ReportStatus status,
        String summary,
        StatsResponseDto stats,
        List<String> positivePoints,
        List<String> negativePoints,
        List<AgeInsightResponseDto> ageInsights
) {
    //상단 통계 바
    @Builder
    public record StatsResponseDto(
            int ageGroupCount,
            long participantCount,
            InputType sourceType,
            Double averageScore
    ) { }

    //연령대별 인사이트 카드
    @Builder
    public record AgeInsightResponseDto(
            AgeGroup ageGroup,
            String displayName,
            Double score,
            Sentiment sentiment,
            String keyPoint,
            String painPoint,
            String improvement
    ) { }
}
