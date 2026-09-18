package kr.yuns.dropthepitchserver.analyze.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.analyze.data.dto.response.FileAnalyzeResponseDto;
import kr.yuns.dropthepitchserver.analyze.service.AnalyzeService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalyzeController {
    private final AnalyzeService analyzeService;

    @GetMapping("/{projectId}")
    @Operation(summary = "파일 분석 결과 조회")
    public GlobalResponse<FileAnalyzeResponseDto> getProjectFileAnalyzeResult(
            @PathVariable("projectId") Long projectId) {
        return GlobalResponse.ok(analyzeService.getProjectFileAnalyzeResult(SecurityUtil.getUsername(), projectId));
    }
}
