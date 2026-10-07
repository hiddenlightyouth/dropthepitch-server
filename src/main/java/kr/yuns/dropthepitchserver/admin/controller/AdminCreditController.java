package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminCreditService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
