package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminCreditHistorySearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditHistoryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminCreditService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/credits")
@RequiredArgsConstructor
public class AdminCreditController {
    private final AdminCreditService adminCreditService;

    @GetMapping("/summary")
    @Operation(summary = "크레딧 전체 합계 조회",
            description = "발행, 사용, 미사용 잔액과 구분별 건수, 합계를 돌려줍니다.")
    public GlobalResponse<AdminCreditSummaryResponseDto> getSummary() {
        return GlobalResponse.ok(adminCreditService.getSummary());
    }

    @GetMapping("/history")
    @Operation(summary = "전체 사용자 크레딧 내역 조회",
            description = "사용자와 프로젝트 검색, 분류, 구분, 거래 시각, 수량 범위, 특정 사용자로 거르고 정렬합니다.")
    public GlobalResponse<AdminPageResponseDto<AdminCreditHistoryResponseDto>> getHistory(
            @ModelAttribute AdminCreditHistorySearchRequestDto request) {
        return GlobalResponse.ok(adminCreditService.getHistory(request));
    }
}
