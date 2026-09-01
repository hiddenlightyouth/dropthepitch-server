package kr.yuns.dropthepitchserver.project.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponseDto(
        Long projectId,
        String title,
        ProjectStatus status,
        LocalDateTime createdAt,
        FileDto file,
        String reportId
) {
    public record FileDto(
            String name,
            Integer size,
            InputType type,
            String url,
            String thumbnailUrl
    ) { }
}
