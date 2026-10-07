package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;

import java.time.LocalDate;

public record AdminAiDailyUsageResponseDto(
        LocalDate date,
        long calls,
        long inputTokens,
        long outputTokens,
        double totalCost
) {
    public static AdminAiDailyUsageResponseDto of(LocalDate date, AiUsageSum sum) {
        return new AdminAiDailyUsageResponseDto(date, sum.getCalls(), sum.getInputTokens(),
                sum.getOutputTokens(), sum.getRoundedCost());
    }
}
