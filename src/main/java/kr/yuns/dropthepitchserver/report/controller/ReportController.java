package kr.yuns.dropthepitchserver.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.report.data.dto.response.ReportSummaryResponseDto;
import kr.yuns.dropthepitchserver.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/{reportId}/summary")
    @Operation(summary = "요약 리포트 결과 조회")
    public GlobalResponse<ReportSummaryResponseDto> getReportSummary(@PathVariable String reportId) {
        return GlobalResponse.ok(reportService.getSummary(SecurityUtil.getUsername(), reportId));
    }
}
