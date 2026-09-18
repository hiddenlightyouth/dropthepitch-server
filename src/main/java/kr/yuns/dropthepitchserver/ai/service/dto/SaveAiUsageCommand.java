package kr.yuns.dropthepitchserver.ai.service.dto;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;

public record SaveAiUsageCommand(
        Long projectId,
        String model,
        AiPurpose purpose,
        Integer inputTokens,
        Integer outputTokens
) {}
