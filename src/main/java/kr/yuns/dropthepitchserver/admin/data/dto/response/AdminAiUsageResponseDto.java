package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminAiUsageResponseDto(
        Long id,
        Long projectId,
        String projectTitle,
        String model,
        AiPurpose purpose,
        int inputTokens,
        int outputTokens,
        double totalCost,
        LocalDateTime requestedAt
) { }
