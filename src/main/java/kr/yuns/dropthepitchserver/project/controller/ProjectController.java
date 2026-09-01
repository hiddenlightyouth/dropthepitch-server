package kr.yuns.dropthepitchserver.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.project.data.dto.response.SidebarProjectResponseDto;
import kr.yuns.dropthepitchserver.project.data.dto.response.ProjectResponseDto;
import kr.yuns.dropthepitchserver.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "사이드바 내 작업 목록 조회")
    public GlobalResponse<List<SidebarProjectResponseDto>> getSidebarProject() {
        return GlobalResponse.ok(projectService.getSidebarProject(SecurityUtil.getUsername()));
    }
    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 조회")
    public GlobalResponse<ProjectResponseDto> getProject(@PathVariable Long projectId) {
        return GlobalResponse.ok(projectService.getProject(SecurityUtil.getUsername(), projectId));
    }
}
