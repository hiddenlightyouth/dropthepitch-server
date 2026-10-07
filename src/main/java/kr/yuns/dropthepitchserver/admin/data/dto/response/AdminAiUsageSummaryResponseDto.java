package kr.yuns.dropthepitchserver.admin.data.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminAiUsageSummaryResponseDto(
        long calls,
        long inputTokens,
        long outputTokens,
        double totalCost,
        long projects,
        double avgCostPerCall,
        LocalDateTime dateMin,
        LocalDateTime dateMax
) { }
