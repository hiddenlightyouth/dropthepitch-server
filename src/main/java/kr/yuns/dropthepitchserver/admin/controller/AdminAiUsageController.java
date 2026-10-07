package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminAiUsageSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiDailyUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiModelUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiProjectUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiPurposeUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiUsageSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminAiUsageService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/daily")
    @Operation(summary = "일별 AI 사용량 조회",
            description = "조건의 기간을 하루씩 돌려줍니다. 기간을 비우면 첫 기록부터 오늘까지입니다.")
    public GlobalResponse<List<AdminAiDailyUsageResponseDto>> getDaily(
            @ModelAttribute AdminAiUsageSearchRequestDto request) {
        return GlobalResponse.ok(adminAiUsageService.getDaily(request));
    }

    @GetMapping("/by-purpose")
    @Operation(summary = "용도별 AI 사용량 조회", description = "파일 분석, 페르소나 선별, 의견 수집, 리포트 생성별 합계를 돌려줍니다.")
    public GlobalResponse<List<AdminAiPurposeUsageResponseDto>> getByPurpose(
            @ModelAttribute AdminAiUsageSearchRequestDto request) {
        return GlobalResponse.ok(adminAiUsageService.getByPurpose(request));
    }

    @GetMapping("/by-model")
    @Operation(summary = "모델별 AI 사용량, 요율 조회", description = "모델별 합계와 100만 토큰당 요율(USD)을 돌려줍니다.")
    public GlobalResponse<List<AdminAiModelUsageResponseDto>> getByModel(
            @ModelAttribute AdminAiUsageSearchRequestDto request) {
        return GlobalResponse.ok(adminAiUsageService.getByModel(request));
    }

    @GetMapping("/by-project")
    @Operation(summary = "프로젝트별 AI 사용량 조회",
            description = "프로젝트마다 용도별 내역을 함께 돌려줍니다. 비용, 토큰, 호출 수, 최근 사용, 용도별 토큰으로 정렬할 수 있습니다.")
    public GlobalResponse<AdminPageResponseDto<AdminAiProjectUsageResponseDto>> getByProject(
            @ModelAttribute AdminAiUsageSearchRequestDto request) {
        return GlobalResponse.ok(adminAiUsageService.getByProject(request));
    }

    @GetMapping
    @Operation(summary = "AI 최근 요청 목록 조회", description = "호출 한 건씩 최근 것부터 돌려줍니다.")
    public GlobalResponse<AdminPageResponseDto<AdminAiUsageResponseDto>> getUsages(
            @ModelAttribute AdminAiUsageSearchRequestDto request) {
        return GlobalResponse.ok(adminAiUsageService.getUsages(request));
    }
}
