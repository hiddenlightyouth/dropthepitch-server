package kr.yuns.dropthepitchserver.report.data.dto.response;

import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportStatus;
import lombok.Builder;

import java.util.List;

//요약 리포트 Dto

@Builder
public record ReportSummaryResponseDto(
        String reportId,
        ReportStatus status,
        String summary,
        String insight,
        List<AgeScoreResponseDto> ageScores
) {
    //연령대별 평균 점수, 의견 수집 전에는 비어 있음
    @Builder
    public record AgeScoreResponseDto(
            AgeGroup ageGroup,
            String displayName,
            Double score
    ) { }
}
