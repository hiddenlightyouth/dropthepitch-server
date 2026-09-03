package kr.yuns.dropthepitchserver.opinion.controller;

import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.opinion.data.dto.response.OpinionListResponseDto;
import kr.yuns.dropthepitchserver.opinion.data.dto.response.PersonaOpinionResponseDto;
import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionSortType;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.opinion.service.OpinionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/opinions")
@RequiredArgsConstructor
public class OpinionController {
    private final OpinionService opinionService;

    @GetMapping("/{opinionsId}")
    public GlobalResponse<PersonaOpinionResponseDto> getPersonaOpinionSummary(@PathVariable Long opinionsId) {
        return GlobalResponse.ok(opinionService.getPersonaOpinionSummary(SecurityUtil.getUsername(), opinionsId));
    }

    @GetMapping
    @Operation(summary = "상세 리포트 의견 목록 조회")
    public GlobalResponse<List<OpinionListResponseDto>> getOpinionList(
            @RequestParam Long projectId,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(required = false, defaultValue = "SCORE_DESC") OpinionSortType sort) {
        return GlobalResponse.ok(opinionService.getOpinionList(SecurityUtil.getUsername(), projectId, ageGroup, sort));
    }
}
