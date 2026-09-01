package kr.yuns.dropthepitchserver.persona.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.persona.data.dto.response.PersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/personas")
@RequiredArgsConstructor
public class PersonaController {
    private final PersonaService personaService;

    @GetMapping("/{personaId}")
    @Operation(summary = "페르소나 상세 정보 조회")
    public GlobalResponse<PersonaResponseDto> getPersona(@PathVariable Long personaId) {
        return GlobalResponse.ok(personaService.getPersona(personaId));
    }
}
