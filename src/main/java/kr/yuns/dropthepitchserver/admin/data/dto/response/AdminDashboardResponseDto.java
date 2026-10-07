package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminDashboardResponseDto(
        UserCount users,
        ProjectCount projects,
        CreditTotal credits,
        AiTotal ai,
        List<DailyPoint> daily,
        List<VerdictCount> verdicts,
        List<AdminProjectResponseDto> recentProjects,
        List<AdminUserResponseDto> recentUsers
) {
    public record UserCount(long total, long newToday, long newInPeriod) { }

    public record ProjectCount(long total, long inProgress, long completed, long failed, long deleted) { }

    public record CreditTotal(long issued, long used, long outstanding) { }

    public record AiTotal(long calls, long inputTokens, long outputTokens, double totalCost) { }

    public record DailyPoint(LocalDate date, long signups, long projects, long creditsUsed) { }

    public record VerdictCount(AnalysisVerdict verdict, long count) { }
}
