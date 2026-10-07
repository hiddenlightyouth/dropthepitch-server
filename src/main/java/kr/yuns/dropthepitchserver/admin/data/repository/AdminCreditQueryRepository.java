package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminCreditHistorySearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditHistoryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto.TypeSummary;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.enums.AdminCreditSort;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Repository
public class AdminCreditQueryRepository {
    private static final String HISTORY = """
            (select concat('F', fc.id) as id,
                    'FILE_ANALYSIS' as type,
                    -fc.use_credit as amount,
                    c.user_id as user_id,
                    fc.project_id as project_id,
                    fc.created_at as occurred_at
               from file_analysis_credit fc
               join credit c on c.id = fc.credit_id
              union all
             select concat('O', oc.id),
                    'OPINION_COLLECTION',
                    -oc.use_credit,
                    c.user_id,
                    oc.project_id,
                    oc.created_at
               from opinion_request_credit oc
               join credit c on c.id = oc.credit_id
              union all
             select concat('P', pay.id),
                    cast(pay.reason as char),
                    pay.amount,
                    c.user_id,
                    null,
                    pay.created_at
               from payment pay
               join credit c on c.id = pay.credit_id) h
            """;

    private static final String SELECT = """
            select h.id as id,
                   h.type as type,
                   h.amount as amount,
                   h.user_id as user_id,
                   u.name as user_name,
                   u.email as user_email,
                   h.project_id as project_id,
                   p.title as project_title,
                   p.deleted_at as project_deleted_at,
                   h.occurred_at as occurred_at
            """;

    private static final String FROM = " from " + HISTORY
            + " join `user` u on u.id = h.user_id left join project p on p.id = h.project_id";

    @PersistenceContext
    private EntityManager entityManager;

    public AdminPageResponseDto<AdminCreditHistoryResponseDto> search(AdminCreditHistorySearchRequestDto request) {
        List<CreditHistoryType> types = Arrays.stream(CreditHistoryType.values())
                .filter(type -> request.getCategory() == null || type.getCategory() == request.getCategory())
                .filter(type -> request.getType() == null || type == request.getType())
                .toList();

        if (types.isEmpty()) {
            return AdminPageResponseDto.of(List.of(), request.getPage(), request.getSize(), 0);
        }

        AdminConditions conditions = new AdminConditions()
                .keyword(request.getKeyword(), "u.name", "u.email", "coalesce(p.title, '')")
                .add(request.getUserId(), "h.user_id = :userId", "userId")
                .period(request.getFrom(), request.getTo(), "h.occurred_at")
                .range(request.getMinAmount(), request.getMaxAmount(), "abs(h.amount)", "amount");

        if (types.size() < CreditHistoryType.values().length) {
            conditions.in(types, "h.type", "types");
        }

        long totalCount = AdminRows.number(entityManager,
                "select count(*)" + FROM + conditions.where(), conditions);

        AdminCreditSort sort = AdminCreditSort.from(request.getSort());
        String direction = request.isAscending() ? " asc" : " desc";
        String orderBy = " order by " + sort.getColumn() + direction + ", h.occurred_at desc, h.id desc";

        List<AdminCreditHistoryResponseDto> items = AdminRows.page(entityManager,
                SELECT + FROM + conditions.where() + orderBy, conditions,
                request.getPage(), request.getSize(), this::toHistory);

        return AdminPageResponseDto.of(items, request.getPage(), request.getSize(), totalCount);
    }

    public List<TypeSummary> sumByType() {
        String sql = "select h.type as type, count(*) as cnt, sum(h.amount) as amount from " + HISTORY
                + " group by h.type";
        return AdminRows.list(entityManager, sql, new AdminConditions(), row -> new TypeSummary(
                        AdminRows.asEnum(row, "type", CreditHistoryType.class),
                        AdminRows.asLongOrZero(row, "cnt"),
                        AdminRows.asLongOrZero(row, "amount")))
                .stream()
                .sorted(Comparator.comparingLong((TypeSummary summary) -> Math.abs(summary.amount())).reversed())
                .toList();
    }

    public long sumOutstanding() {
        return AdminRows.number(entityManager, "select coalesce(sum(c.amount), 0) from credit c",
                new AdminConditions());
    }

    private AdminCreditHistoryResponseDto toHistory(Tuple row) {
        CreditHistoryType type = AdminRows.asEnum(row, "type", CreditHistoryType.class);
        return AdminCreditHistoryResponseDto.builder()
                .id(AdminRows.asString(row, "id"))
                .type(type)
                .category(type.getCategory())
                .amount(AdminRows.asInt(row, "amount"))
                .userId(AdminRows.asLong(row, "user_id"))
                .userName(AdminRows.asString(row, "user_name"))
                .userEmail(AdminRows.asString(row, "user_email"))
                .projectId(AdminRows.asLong(row, "project_id"))
                .projectTitle(AdminRows.asString(row, "project_title"))
                .projectDeleted(AdminRows.asDateTime(row, "project_deleted_at") != null)
                .occurredAt(AdminRows.asDateTime(row, "occurred_at"))
                .build();
    }
}
