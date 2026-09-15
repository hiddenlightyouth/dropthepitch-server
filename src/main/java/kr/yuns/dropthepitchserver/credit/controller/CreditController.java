package kr.yuns.dropthepitchserver.credit.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditResponseDto;
import kr.yuns.dropthepitchserver.credit.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {
    private final CreditService creditService;

    @GetMapping
    @Operation(summary = "보유 크레딧 조회", description = "크레딧 정보가 없는 사용자 0으로 처리.")
    public GlobalResponse<CreditResponseDto> getCredit() {
        return GlobalResponse.ok(creditService.getCredit(SecurityUtil.getUsername()));
    }
}
