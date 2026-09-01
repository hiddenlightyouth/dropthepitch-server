package kr.yuns.dropthepitchserver.project.data.dto.response;

import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SidebarProjectResponseDto {
    private Long projectId;
    private String title;
    private ProjectStatus status;
}
