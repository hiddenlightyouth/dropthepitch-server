package kr.yuns.dropthepitchserver.credit.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditHistoryResponseDto;
import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditResponseDto;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryCategory;
import kr.yuns.dropthepitchserver.credit.service.CreditHistoryService;
import kr.yuns.dropthepitchserver.credit.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {
    private final CreditService creditService;
    private final CreditHistoryService creditHistoryService;

    @GetMapping
    @Operation(summary = "보유 크레딧 조회", description = "크레딧 정보가 없는 사용자 0으로 처리.")
    public GlobalResponse<CreditResponseDto> getCredit() {
        return GlobalResponse.ok(creditService.getCredit(SecurityUtil.getUsername()));
    }

    @GetMapping("/history")
    @Operation(summary = "크레딧 내역 조회",
            description = "사용, 충전, 적립 내역을 최신순으로 돌려줍니다. category를 주면 해당 탭만 나갑니다.")
    public GlobalResponse<List<CreditHistoryResponseDto>> getHistory(
            @RequestParam(required = false) CreditHistoryCategory category) {
        return GlobalResponse.ok(creditHistoryService.getHistory(SecurityUtil.getUsername(), category));
    }
}
