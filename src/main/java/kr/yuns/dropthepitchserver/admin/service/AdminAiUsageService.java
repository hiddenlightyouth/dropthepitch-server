package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminAiUsageSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiUsageSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository.ProjectPurposeUsage;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository.Span;
import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAiUsageService {
    private final AdminAiUsageQueryRepository adminAiUsageQueryRepository;

    private static class ProjectUsage {
        private final Long projectId;
        private final AiUsageSum total = new AiUsageSum();
        private final Map<AiPurpose, AiUsageSum> purposes = new EnumMap<>(AiPurpose.class);
        private LocalDateTime lastUsedAt;

        private ProjectUsage(Long projectId) {
            this.projectId = projectId;
        }

        private void add(ProjectPurposeUsage usage) {
            total.add(usage.model(), usage.calls(), usage.inputTokens(), usage.outputTokens());
            purposes.computeIfAbsent(usage.purpose(), purpose -> new AiUsageSum())
                    .add(usage.model(), usage.calls(), usage.inputTokens(), usage.outputTokens());
            if (lastUsedAt == null || (usage.lastUsedAt() != null && usage.lastUsedAt().isAfter(lastUsedAt))) {
                lastUsedAt = usage.lastUsedAt();
            }
        }

        private long tokensOf(AiPurpose purpose) {
            AiUsageSum sum = purposes.get(purpose);
            return sum == null ? 0 : sum.getTotalTokens();
        }
    }

    @Transactional(readOnly = true)
    public AdminAiUsageSummaryResponseDto getSummary(AdminAiUsageSearchRequestDto request) {
        AiUsageSum total = adminAiUsageQueryRepository.sumAll(request);
        Span span = adminAiUsageQueryRepository.findSpan(request);

        log.info("[getSummary] AI 사용량 합계 조회: 호출 {}건, 비용 {}", total.getCalls(), total.getRoundedCost());

        return AdminAiUsageSummaryResponseDto.builder()
                .calls(total.getCalls())
                .inputTokens(total.getInputTokens())
                .outputTokens(total.getOutputTokens())
                .totalCost(total.getRoundedCost())
                .projects(span.projects())
                .avgCostPerCall(total.getCalls() == 0 ? 0
                        : Math.round(total.getTotalCost() / total.getCalls() * 1_000_000) / 1_000_000.0)
                .dateMin(span.dateMin())
                .dateMax(span.dateMax())
                .build();
    }
}
