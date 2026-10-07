package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminAiUsageSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiDailyUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiModelUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiProjectUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiPurposeUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiUsageSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository.ProjectMeta;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository.ProjectPurposeUsage;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminAiUsageQueryRepository.Span;
import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;
import kr.yuns.dropthepitchserver.admin.support.GeminiPricing;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAiUsageService {
    private static final int MAX_DAILY_POINTS = 366;

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

    @Transactional(readOnly = true)
    public List<AdminAiDailyUsageResponseDto> getDaily(AdminAiUsageSearchRequestDto request) {
        Map<LocalDate, AiUsageSum> sums = adminAiUsageQueryRepository.sumByDate(request);

        LocalDate today = LocalDate.now();
        LocalDate to = request.getTo() != null ? request.getTo() : today;
        LocalDate from = request.getFrom() != null ? request.getFrom()
                : sums.keySet().stream().min(Comparator.naturalOrder()).orElse(to);

        if (from.isAfter(to)) {
            return List.of();
        }
        if (from.isBefore(to.minusDays(MAX_DAILY_POINTS - 1))) {
            from = to.minusDays(MAX_DAILY_POINTS - 1);
        }

        List<AdminAiDailyUsageResponseDto> daily = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            daily.add(AdminAiDailyUsageResponseDto.of(date, sums.getOrDefault(date, new AiUsageSum())));
        }
        return daily;
    }

    @Transactional(readOnly = true)
    public List<AdminAiPurposeUsageResponseDto> getByPurpose(AdminAiUsageSearchRequestDto request) {
        return adminAiUsageQueryRepository.sumByPurpose(request).entrySet().stream()
                .map(entry -> AdminAiPurposeUsageResponseDto.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(AdminAiPurposeUsageResponseDto::totalCost).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAiModelUsageResponseDto> getByModel(AdminAiUsageSearchRequestDto request) {
        return adminAiUsageQueryRepository.sumByModel(request).entrySet().stream()
                .map(entry -> {
                    String model = entry.getKey();
                    AiUsageSum sum = entry.getValue();
                    return AdminAiModelUsageResponseDto.builder()
                            .model(model)
                            .displayName(GeminiPricing.displayNameOf(model))
                            .inputRate(GeminiPricing.rateOf(model).input())
                            .outputRate(GeminiPricing.rateOf(model).output())
                            .calls(sum.getCalls())
                            .inputTokens(sum.getInputTokens())
                            .outputTokens(sum.getOutputTokens())
                            .totalCost(sum.getRoundedCost())
                            .build();
                })
                .sorted(Comparator.comparingDouble(AdminAiModelUsageResponseDto::totalCost).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminAiProjectUsageResponseDto> getByProject(AdminAiUsageSearchRequestDto request) {
        Map<Long, ProjectUsage> usages = new LinkedHashMap<>();
        adminAiUsageQueryRepository.findProjectUsages(request).forEach(usage ->
                usages.computeIfAbsent(usage.projectId(), ProjectUsage::new).add(usage));

        Comparator<ProjectUsage> comparator = Comparator.comparingDouble(sortKeyOf(request.getSort()));
        if (!request.isAscending()) {
            comparator = comparator.reversed();
        }

        List<ProjectUsage> sorted = usages.values().stream()
                .sorted(comparator.thenComparing(usage -> usage.projectId, Comparator.reverseOrder()))
                .toList();

        List<ProjectUsage> pageItems = sorted.stream()
                .skip((long) request.getPage() * request.getSize())
                .limit(request.getSize())
                .toList();

        Map<Long, ProjectMeta> metas = adminAiUsageQueryRepository.findProjectMetas(
                pageItems.stream().map(usage -> usage.projectId).toList());

        List<AdminAiProjectUsageResponseDto> items = pageItems.stream()
                .map(usage -> toProjectUsage(usage, metas.get(usage.projectId)))
                .toList();

        log.info("[getByProject] 프로젝트별 AI 사용량 조회: 전체 {}건, page={}", sorted.size(), request.getPage());

        return AdminPageResponseDto.of(items, request.getPage(), request.getSize(), sorted.size());
    }

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminAiUsageResponseDto> getUsages(AdminAiUsageSearchRequestDto request) {
        return adminAiUsageQueryRepository.search(request);
    }

    private ToDoubleFunction<ProjectUsage> sortKeyOf(String sort) {
        if ("totalTokens".equals(sort)) {
            return usage -> usage.total.getTotalTokens();
        }
        if ("calls".equals(sort)) {
            return usage -> usage.total.getCalls();
        }
        if ("lastUsedAt".equals(sort)) {
            return usage -> usage.lastUsedAt == null ? 0
                    : usage.lastUsedAt.toEpochSecond(java.time.ZoneOffset.UTC);
        }
        return Arrays.stream(AiPurpose.values())
                .filter(purpose -> purpose.name().equals(sort))
                .findFirst()
                .<ToDoubleFunction<ProjectUsage>>map(purpose -> usage -> usage.tokensOf(purpose))
                .orElse(usage -> usage.total.getTotalCost());
    }

    private AdminAiProjectUsageResponseDto toProjectUsage(ProjectUsage usage, ProjectMeta meta) {
        List<AdminAiPurposeUsageResponseDto> purposes = usage.purposes.entrySet().stream()
                .map(entry -> AdminAiPurposeUsageResponseDto.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(AdminAiPurposeUsageResponseDto::totalCost).reversed())
                .toList();

        return AdminAiProjectUsageResponseDto.builder()
                .projectId(usage.projectId)
                .title(meta == null ? null : meta.title())
                .status(meta == null ? null : meta.status())
                .deleted(meta != null && meta.deleted())
                .fileName(meta == null ? null : meta.fileName())
                .fileType(meta == null ? null : meta.fileType())
                .fileSize(meta == null ? null : meta.fileSize())
                .lastUsedAt(usage.lastUsedAt)
                .purposes(purposes)
                .calls(usage.total.getCalls())
                .inputTokens(usage.total.getInputTokens())
                .outputTokens(usage.total.getOutputTokens())
                .totalCost(usage.total.getRoundedCost())
                .build();
    }
}
