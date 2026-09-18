package kr.yuns.dropthepitchserver.project.data.dto.response;

import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SidebarProjectResponseDto(
        Long projectId,
        String title,
        ProjectStatus status,
        LocalDateTime date
) {}
