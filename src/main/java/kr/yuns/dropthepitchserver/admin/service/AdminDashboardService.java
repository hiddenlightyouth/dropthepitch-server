package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminAiUsageSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto.AiTotal;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto.CreditTotal;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto.DailyPoint;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminDashboardQueryRepository;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminProjectQueryRepository;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminUserQueryRepository;
import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDashboardService {
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 90;
    private static final int RECENT_LIMIT = 6;

    private final AdminDashboardQueryRepository adminDashboardQueryRepository;
    private final AdminUserQueryRepository adminUserQueryRepository;
    private final AdminProjectQueryRepository adminProjectQueryRepository;
    private final AdminAiUsageQueryRepository adminAiUsageQueryRepository;
    private final AdminCreditService adminCreditService;

    @Transactional(readOnly = true)
    public AdminDashboardResponseDto getDashboard(int days) {
        int period = Math.min(Math.max(days, MIN_DAYS), MAX_DAYS);
        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.minusDays(period - 1L);

        AdminCreditSummaryResponseDto credits = adminCreditService.getSummary();

        AdminAiUsageSearchRequestDto aiRequest = new AdminAiUsageSearchRequestDto();
        aiRequest.setFrom(periodStart);
        AiUsageSum ai = adminAiUsageQueryRepository.sumAll(aiRequest);

        log.info("[getDashboard] 대시보드 조회: 최근 {}일", period);

        return AdminDashboardResponseDto.builder()
                .users(adminDashboardQueryRepository.countUsers(periodStart, today))
                .projects(adminDashboardQueryRepository.countProjects())
                .credits(new CreditTotal(credits.issued(), credits.used(), credits.outstanding()))
                .ai(new AiTotal(ai.getCalls(), ai.getInputTokens(), ai.getOutputTokens(), ai.getRoundedCost()))
                .daily(toDaily(periodStart, today))
                .verdicts(adminDashboardQueryRepository.countVerdicts(periodStart))
                .recentProjects(adminProjectQueryRepository.findRecent(RECENT_LIMIT))
                .recentUsers(adminUserQueryRepository.findRecent(RECENT_LIMIT))
                .build();
    }

    private List<DailyPoint> toDaily(LocalDate periodStart, LocalDate today) {
        Map<LocalDate, Long> signups = adminDashboardQueryRepository.countSignupsByDate(periodStart);
        Map<LocalDate, Long> projects = adminDashboardQueryRepository.countProjectsByDate(periodStart);
        Map<LocalDate, Long> creditsUsed = adminDashboardQueryRepository.sumUsedCreditsByDate(periodStart);

        List<DailyPoint> daily = new ArrayList<>();
        for (LocalDate date = periodStart; !date.isAfter(today); date = date.plusDays(1)) {
            daily.add(new DailyPoint(date,
                    signups.getOrDefault(date, 0L),
                    projects.getOrDefault(date, 0L),
                    creditsUsed.getOrDefault(date, 0L)));
        }
        return daily;
    }
}
