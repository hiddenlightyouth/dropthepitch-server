package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminAiUsageSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Repository
public class AdminAiUsageQueryRepository {
    private static final String DELETED = "DELETED";

    private static final String FROM = """
             from ai_usage a
             join project p on p.id = a.project_id
             join `user` u on u.id = p.user_id
             left join file f on f.project_id = p.id
            """;

    private static final String SUMS = """
            , a.model as model,
              count(*) as calls,
              sum(a.input_tokens) as input_tokens,
              sum(a.output_tokens) as output_tokens,
              max(a.requested_at) as last_used_at
            """;

    public record Span(long projects, LocalDateTime dateMin, LocalDateTime dateMax) { }

    public record ProjectPurposeUsage(Long projectId, AiPurpose purpose, String model, long calls,
                                      long inputTokens, long outputTokens, LocalDateTime lastUsedAt) { }

    public record ProjectMeta(Long projectId, String title, ProjectStatus status, boolean deleted,
                              String fileName, InputType fileType, Long fileSize) { }

    @PersistenceContext
    private EntityManager entityManager;

    public AiUsageSum sumAll(AdminAiUsageSearchRequestDto request) {
        AiUsageSum total = new AiUsageSum();
        sumBy(request, "a.model", row -> AdminRows.asString(row, "group_key")).values()
                .forEach(total::merge);
        return total;
    }

    public Map<AiPurpose, AiUsageSum> sumByPurpose(AdminAiUsageSearchRequestDto request) {
        return sumBy(request, "a.purpose", row -> AdminRows.asEnum(row, "group_key", AiPurpose.class));
    }

    public Map<LocalDate, AiUsageSum> sumByDate(AdminAiUsageSearchRequestDto request) {
        return sumBy(request, "cast(a.requested_at as date)", row -> AdminRows.asDate(row, "group_key"));
    }

    public Span findSpan(AdminAiUsageSearchRequestDto request) {
        AdminConditions conditions = conditionsOf(request);
        String sql = "select count(distinct a.project_id) as projects, min(a.requested_at) as date_min,"
                + " max(a.requested_at) as date_max" + FROM + conditions.where();
        return AdminRows.list(entityManager, sql, conditions, row -> new Span(
                AdminRows.asLongOrZero(row, "projects"),
                AdminRows.asDateTime(row, "date_min"),
                AdminRows.asDateTime(row, "date_max"))).getFirst();
    }

    private <K> Map<K, AiUsageSum> sumBy(AdminAiUsageSearchRequestDto request, String keyExpression,
                                         Function<Tuple, K> keyOf) {
        AdminConditions conditions = conditionsOf(request);
        String sql = "select " + keyExpression + " as group_key" + SUMS + FROM + conditions.where()
                + " group by " + keyExpression + ", a.model";

        Map<K, AiUsageSum> sums = new LinkedHashMap<>();
        AdminRows.list(entityManager, sql, conditions, row -> row).forEach(row ->
                sums.computeIfAbsent(keyOf.apply(row), key -> new AiUsageSum())
                        .add(AdminRows.asString(row, "model"),
                                AdminRows.asLongOrZero(row, "calls"),
                                AdminRows.asLongOrZero(row, "input_tokens"),
                                AdminRows.asLongOrZero(row, "output_tokens")));
        return sums;
    }

    private AdminConditions conditionsOf(AdminAiUsageSearchRequestDto request) {
        AdminConditions conditions = new AdminConditions()
                .period(request.getFrom(), request.getTo(), "a.requested_at")
                .add(request.getPurpose(), "a.purpose = :purpose", "purpose")
                .add(blankToNull(request.getModel()), "a.model = :model", "model")
                .add(request.getFileType(), "f.type = :fileType", "fileType")
                .keyword(request.getKeyword(), "p.title", "coalesce(f.name, '')", "u.name", "u.email");

        String status = blankToNull(request.getStatus());
        if (DELETED.equals(status)) {
            conditions.add("p.deleted_at is not null");
        } else if (status != null
                && Arrays.stream(ProjectStatus.values()).anyMatch(value -> value.name().equals(status))) {
            conditions.add(status, "p.status = :status", "status");
        }
        return conditions;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
