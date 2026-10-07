package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminUserSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminUserService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
}
