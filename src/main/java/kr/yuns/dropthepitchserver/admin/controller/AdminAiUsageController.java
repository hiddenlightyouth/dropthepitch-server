package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminAiUsageSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiUsageSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminAiUsageService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ai-usage")
@RequiredArgsConstructor
public class AdminAiUsageController {
    private final AdminAiUsageService adminAiUsageService;

    @GetMapping("/summary")
    @Operation(summary = "AI 사용량 합계 조회",
            description = "검색 조건에 맞는 호출의 비용(USD), 토큰, 호출 수, 프로젝트 수를 돌려줍니다.")
    public GlobalResponse<AdminAiUsageSummaryResponseDto> getSummary(
            @ModelAttribute AdminAiUsageSearchRequestDto request) {
        return GlobalResponse.ok(adminAiUsageService.getSummary(request));
    }
}
