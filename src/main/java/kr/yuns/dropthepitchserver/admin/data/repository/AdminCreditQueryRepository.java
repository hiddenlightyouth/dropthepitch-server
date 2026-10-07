package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto.TypeSummary;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import org.springframework.stereotype.Repository;

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

    @PersistenceContext
    private EntityManager entityManager;

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
}
