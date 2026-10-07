package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminAiProjectUsageResponseDto(
        Long projectId,
        String title,
        ProjectStatus status,
        boolean deleted,
        String fileName,
        InputType fileType,
        Long fileSize,
        LocalDateTime lastUsedAt,
        List<AdminAiPurposeUsageResponseDto> purposes,
        long calls,
        long inputTokens,
        long outputTokens,
        double totalCost
) { }
