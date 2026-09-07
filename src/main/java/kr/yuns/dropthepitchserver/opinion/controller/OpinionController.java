package kr.yuns.dropthepitchserver.opinion.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.opinion.data.dto.response.PersonaOpinionResponseDto;
import kr.yuns.dropthepitchserver.opinion.service.OpinionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opinions")
@RequiredArgsConstructor
public class OpinionController {
    private final OpinionService opinionService;

    @GetMapping("/{opinionsId}")
    @Operation(summary = "페르소나 한 줄 의견 조회")
    public GlobalResponse<PersonaOpinionResponseDto> getPersonaOpinionSummary(@PathVariable Long opinionsId) {
        return GlobalResponse.ok(opinionService.getPersonaOpinionSummary(SecurityUtil.getUsername(), opinionsId));
    }
}
