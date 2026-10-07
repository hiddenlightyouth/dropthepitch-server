package kr.yuns.dropthepitchserver.admin.data.dto.response;

import lombok.Builder;

@Builder
public record AdminAiModelUsageResponseDto(
        String model,
        String displayName,
        double inputRate,
        double outputRate,
        long calls,
        long inputTokens,
        long outputTokens,
        double totalCost
) { }
