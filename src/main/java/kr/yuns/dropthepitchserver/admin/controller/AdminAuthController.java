package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminMeResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminUserService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminUserService adminUserService;

    @GetMapping("/me")
    @Operation(summary = "관리자 계정 확인",
            description = "로그인한 계정의 이메일, 이름, 역할을 돌려줍니다. 관리자가 아니면 이 경로에 닿기 전에 403이 됩니다.")
    public GlobalResponse<AdminMeResponseDto> getMe() {
        return GlobalResponse.ok(adminUserService.getMe(SecurityUtil.getUsername()));
    }
}
