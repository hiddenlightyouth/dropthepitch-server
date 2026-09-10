package kr.yuns.dropthepitchserver.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.project.data.dto.request.UpdateTitleRequestDto;
import kr.yuns.dropthepitchserver.project.data.dto.response.OpinionStatusResponseDto;
import kr.yuns.dropthepitchserver.persona.data.dto.response.SelectedPersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.service.PersonaService;
import kr.yuns.dropthepitchserver.project.data.dto.response.ProjectStatusResponseDto;
import kr.yuns.dropthepitchserver.project.data.dto.response.SidebarProjectResponseDto;
import kr.yuns.dropthepitchserver.project.data.dto.response.ProjectResponseDto;
import kr.yuns.dropthepitchserver.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final PersonaService personaService;

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
    @PatchMapping("/{projectId}")
    @Operation(summary = "프로젝트 이름 변경")
    public GlobalResponse<SidebarProjectResponseDto> editTitle(@PathVariable Long projectId,
                                                               @RequestBody UpdateTitleRequestDto request) {
        return GlobalResponse.ok(
                projectService.changeTitle(SecurityUtil.getUsername(), projectId, request.getTitle()));
    }

    @GetMapping("/{projectId}/status")
    @Operation(summary = "프로젝트 상태 조회")
    public GlobalResponse<ProjectStatusResponseDto> getProjectStatus(@PathVariable Long projectId) {
        return GlobalResponse.ok(projectService.getProjectStatus(SecurityUtil.getUsername(), projectId));
    }

    @GetMapping("/{projectId}/opinions/status")
    @Operation(summary = "프로젝트 의견 수집 상태 조회")
    public GlobalResponse<OpinionStatusResponseDto> getOpinionStatus(@PathVariable Long projectId) {
        return GlobalResponse.ok(projectService.getOpinionStatus(SecurityUtil.getUsername(), projectId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "새 작업 시작(파일 업로드)")
    public GlobalResponse<ProjectResponseDto> createProject(@RequestPart("file") MultipartFile file) {
        return GlobalResponse.ok(projectService.createProject(SecurityUtil.getUsername(), file));
    }

    @GetMapping("/{projectId}/personas")
    @Operation(summary = "선정된 페르소나 목록 조회", description = "선별이 끝나기 전에는 빈 목록이 나갑니다.")
    public GlobalResponse<List<SelectedPersonaResponseDto>> getSelectedPersonas(@PathVariable Long projectId) {
        return GlobalResponse.ok(personaService.getSelectedPersonas(SecurityUtil.getUsername(), projectId));
    }

    @PostMapping("/{projectId}/personas/{personaId}/replace")
    @Operation(summary = "선정된 페르소나 교체", description = "같은 연령대의 다른 페르소나로 바꿉니다. 의견 수집이 시작된 뒤에는 교체할 수 없습니다.")
    public GlobalResponse<SelectedPersonaResponseDto> replacePersona(@PathVariable Long projectId,
                                                                     @PathVariable Long personaId) {
        return GlobalResponse.ok(personaService.replacePersona(SecurityUtil.getUsername(), projectId, personaId));
    }

}
