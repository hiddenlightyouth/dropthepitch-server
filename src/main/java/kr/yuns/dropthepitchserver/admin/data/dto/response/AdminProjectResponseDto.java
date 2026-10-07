package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminProjectResponseDto(
        Long id,
        String title,
        ProjectStatus status,
        OpinionCollectionStatus opinionCollectionStatus,
        InputType inputType,
        Long userId,
        String userName,
        String userEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        long usedCredit,
        Double averageScore
) { }
