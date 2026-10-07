package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminDashboardService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @Operation(summary = "대시보드 요약 조회",
            description = "사용자 수, 프로젝트 수, 크레딧 현황, 최근 기간의 AI 사용량과 일별 추이, 분석 거절 사유별 건수, 최근 프로젝트와 가입 사용자를 돌려줍니다.")
    public GlobalResponse<AdminDashboardResponseDto> getDashboard(@RequestParam(defaultValue = "14") int days) {
        return GlobalResponse.ok(adminDashboardService.getDashboard(days));
    }
}
