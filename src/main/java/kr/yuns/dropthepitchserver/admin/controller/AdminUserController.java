package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminCreditGrantRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminRoleChangeRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminUserSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminUserService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "사용자 목록 조회",
            description = "이름과 이메일 검색, 역할, 가입일, 보유 크레딧 범위, 프로젝트 유무로 거르고 정렬합니다.")
    public GlobalResponse<AdminPageResponseDto<AdminUserResponseDto>> getUsers(
            @ModelAttribute AdminUserSearchRequestDto request) {
        return GlobalResponse.ok(adminUserService.getUsers(request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 상세 조회", description = "받은 크레딧, 사용한 크레딧, 마지막 프로젝트 시각을 함께 돌려줍니다.")
    public GlobalResponse<AdminUserDetailResponseDto> getUser(@PathVariable Long userId) {
        return GlobalResponse.ok(adminUserService.getUser(userId));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "사용자 역할 변경", description = "자신의 역할은 바꿀 수 없습니다.")
    public GlobalResponse<Void> changeRole(@PathVariable Long userId,
                                           @Valid @RequestBody AdminRoleChangeRequestDto request) {
        adminUserService.changeRole(SecurityUtil.getUsername(), userId, request);
        return GlobalResponse.ok();
    }

    @PostMapping("/{userId}/credits")
    @Operation(summary = "사용자 크레딧 지급", description = "보유 크레딧이 늘고 내역에 관리자 지급으로 남습니다.")
    public GlobalResponse<Void> grantCredit(@PathVariable Long userId,
                                            @Valid @RequestBody AdminCreditGrantRequestDto request) {
        adminUserService.grantCredit(SecurityUtil.getUsername(), userId, request);
        return GlobalResponse.ok();
    }
}
