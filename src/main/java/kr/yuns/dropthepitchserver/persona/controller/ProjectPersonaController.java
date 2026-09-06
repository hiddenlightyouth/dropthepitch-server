package kr.yuns.dropthepitchserver.persona.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.persona.data.dto.response.SelectedPersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//프로젝트에 선정된 페르소나를 다룬다. 전체 페르소나를 다루는 PersonaController와 경로가 달라 나눔.
@RestController
@RequestMapping("/projects/{projectId}/personas")
@RequiredArgsConstructor
public class ProjectPersonaController {
    private final PersonaService personaService;

    @GetMapping
    @Operation(summary = "선정된 페르소나 목록 조회", description = "선별이 끝나기 전에는 빈 목록이 나갑니다.")
    public GlobalResponse<List<SelectedPersonaResponseDto>> getSelectedPersonas(@PathVariable Long projectId) {
        return GlobalResponse.ok(personaService.getSelectedPersonas(SecurityUtil.getUsername(), projectId));
    }
}
