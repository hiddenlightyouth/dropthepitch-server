package kr.yuns.dropthepitchserver.opinion.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.opinion.service.OpinionCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/opinions")
@RequiredArgsConstructor
public class OpinionCollectionController {
    private final OpinionCollectionService opinionCollectionService;

    @PostMapping("/collect")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "프로젝트 페르소나 의견 수집 시작")
    public GlobalResponse<Integer> collectAllOpinions(@PathVariable Long projectId) {
        return GlobalResponse.ok(opinionCollectionService.collectAllOpinions(SecurityUtil.getUsername(), projectId));
    }
}
