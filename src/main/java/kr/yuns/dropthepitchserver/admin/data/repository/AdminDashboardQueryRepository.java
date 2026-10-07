package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto.ProjectCount;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto.UserCount;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminDashboardResponseDto.VerdictCount;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AdminDashboardQueryRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public UserCount countUsers(LocalDate periodStart, LocalDate today) {
        long total = AdminRows.number(entityManager, "select count(*) from `user` u", new AdminConditions());
        return new UserCount(total, countUsersSince(today), countUsersSince(periodStart));
    }

    public ProjectCount countProjects() {
        String sql = """
                select p.status as status,
                       case when p.deleted_at is null then 0 else 1 end as deleted,
                       count(*) as cnt
                  from project p
                 group by p.status, case when p.deleted_at is null then 0 else 1 end
                """;

        long inProgress = 0;
        long completed = 0;
        long failed = 0;
        long deleted = 0;
        for (var row : AdminRows.list(entityManager, sql, new AdminConditions(), row -> row)) {
            long count = AdminRows.asLongOrZero(row, "cnt");
            if (AdminRows.asInt(row, "deleted") == 1) {
                deleted += count;
                continue;
            }
            ProjectStatus status = AdminRows.asEnum(row, "status", ProjectStatus.class);
            if (status == ProjectStatus.IN_PROGRESS) {
                inProgress += count;
            } else if (status == ProjectStatus.COMPLETED) {
                completed += count;
            } else if (status == ProjectStatus.FAILED) {
                failed += count;
            }
        }
        return new ProjectCount(inProgress + completed + failed, inProgress, completed, failed, deleted);
    }

    public Map<LocalDate, Long> countSignupsByDate(LocalDate periodStart) {
        return countByDate("select cast(u.registered_at as date) as day, count(*) as value from `user` u"
                + " where u.registered_at >= :periodStart group by cast(u.registered_at as date)", periodStart);
    }

    public Map<LocalDate, Long> countProjectsByDate(LocalDate periodStart) {
        return countByDate("select cast(p.created_at as date) as day, count(*) as value from project p"
                + " where p.created_at >= :periodStart group by cast(p.created_at as date)", periodStart);
    }

    public Map<LocalDate, Long> sumUsedCreditsByDate(LocalDate periodStart) {
        return countByDate("""
                select cast(h.created_at as date) as day, sum(h.use_credit) as value
                  from (select fc.created_at as created_at, fc.use_credit as use_credit from file_analysis_credit fc
                         union all
                        select oc.created_at, oc.use_credit from opinion_request_credit oc) h
                 where h.created_at >= :periodStart
                 group by cast(h.created_at as date)
                """, periodStart);
    }

    public List<VerdictCount> countVerdicts(LocalDate periodStart) {
        AdminConditions conditions = new AdminConditions().param("periodStart", periodStart.atStartOfDay());
        String sql = """
                select a.reject_reason as verdict, count(*) as cnt
                  from analysis a
                 where a.reject_reason is not null
                   and a.reject_reason <> 'ANALYZABLE'
                   and a.created_at >= :periodStart
                 group by a.reject_reason
                """;
        return AdminRows.list(entityManager, sql, conditions, row -> new VerdictCount(
                        AdminRows.asEnum(row, "verdict", AnalysisVerdict.class),
                        AdminRows.asLongOrZero(row, "cnt")))
                .stream()
                .filter(count -> count.verdict() != null)
                .sorted(Comparator.comparingLong(VerdictCount::count).reversed())
                .toList();
    }

    private long countUsersSince(LocalDate date) {
        AdminConditions conditions = new AdminConditions().param("since", date.atStartOfDay());
        return AdminRows.number(entityManager,
                "select count(*) from `user` u where u.registered_at >= :since", conditions);
    }

    private Map<LocalDate, Long> countByDate(String sql, LocalDate periodStart) {
        AdminConditions conditions = new AdminConditions().param("periodStart", periodStart.atStartOfDay());
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        AdminRows.list(entityManager, sql, conditions, row -> row).forEach(row ->
                counts.put(AdminRows.asDate(row, "day"), AdminRows.asLongOrZero(row, "value")));
        return counts;
    }
}
