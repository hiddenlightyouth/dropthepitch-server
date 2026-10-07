package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;

public record AdminAiPurposeUsageResponseDto(
        AiPurpose purpose,
        long calls,
        long inputTokens,
        long outputTokens,
        double totalCost
) {
    public static AdminAiPurposeUsageResponseDto of(AiPurpose purpose, AiUsageSum sum) {
        return new AdminAiPurposeUsageResponseDto(purpose, sum.getCalls(), sum.getInputTokens(),
                sum.getOutputTokens(), sum.getRoundedCost());
    }
}
