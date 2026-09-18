package kr.yuns.dropthepitchserver.project.data.dto.response;

import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import lombok.Builder;

@Builder
public record ProjectStatusResponseDto(
        ProjectStatus status,
        String statusDisplay
) {
}
