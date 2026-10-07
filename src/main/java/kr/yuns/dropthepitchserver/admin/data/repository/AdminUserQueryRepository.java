package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminUserSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserResponseDto;
import kr.yuns.dropthepitchserver.admin.data.enums.AdminUserSort;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.user.data.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdminUserQueryRepository {
    private static final String CREDIT = "coalesce(c.amount, 0)";
    private static final String PROJECT_COUNT =
            "(select count(*) from project p where p.user_id = u.id and p.deleted_at is null)";

    private static final String SELECT = """
            select u.id as id,
                   u.email as email,
                   u.name as name,
                   u.role as role,
                   u.registered_at as registered_at,
                   u.modified_at as modified_at,
                   %s as credit,
                   %s as project_count
            """.formatted(CREDIT, PROJECT_COUNT);

    private static final String FROM = " from `user` u left join credit c on c.user_id = u.id";

    @PersistenceContext
    private EntityManager entityManager;

    public AdminPageResponseDto<AdminUserResponseDto> search(AdminUserSearchRequestDto request) {
        AdminConditions conditions = new AdminConditions()
                .keyword(request.getKeyword(), "u.name", "u.email")
                .add(request.getRole(), "u.role = :role", "role")
                .period(request.getFrom(), request.getTo(), "u.registered_at")
                .range(request.getMinCredit(), request.getMaxCredit(), CREDIT, "credit");

        if (request.getHasProject() != null) {
            conditions.add(PROJECT_COUNT + (request.getHasProject() ? " > 0" : " = 0"));
        }

        long totalCount = AdminRows.number(entityManager,
                "select count(*)" + FROM + conditions.where(), conditions);

        AdminUserSort sort = AdminUserSort.from(request.getSort());
        String orderBy = " order by " + sort.getColumn() + (request.isAscending() ? " asc" : " desc") + ", u.id desc";

        List<AdminUserResponseDto> items = AdminRows.page(entityManager,
                SELECT + FROM + conditions.where() + orderBy, conditions,
                request.getPage(), request.getSize(), this::toSummary);

        return AdminPageResponseDto.of(items, request.getPage(), request.getSize(), totalCount);
    }

    public Optional<AdminUserDetailResponseDto> findDetail(Long userId) {
        AdminConditions conditions = new AdminConditions().add(userId, "u.id = :userId", "userId");
        String sql = SELECT + """
                ,
                       (select coalesce(sum(pay.amount), 0) from payment pay where pay.credit_id = c.id) as credit_earned,
                       (select coalesce(sum(fc.use_credit), 0) from file_analysis_credit fc where fc.credit_id = c.id)
                         + (select coalesce(sum(oc.use_credit), 0) from opinion_request_credit oc where oc.credit_id = c.id)
                         as credit_used,
                       (select max(p.created_at) from project p where p.user_id = u.id) as last_project_at
                """ + FROM + conditions.where();

        return AdminRows.list(entityManager, sql, conditions, this::toDetail).stream().findFirst();
    }

    private AdminUserResponseDto toSummary(Tuple row) {
        return AdminUserResponseDto.builder()
                .id(AdminRows.asLong(row, "id"))
                .email(AdminRows.asString(row, "email"))
                .name(AdminRows.asString(row, "name"))
                .role(AdminRows.asEnum(row, "role", UserRole.class))
                .registeredAt(AdminRows.asDateTime(row, "registered_at"))
                .credit(AdminRows.asInt(row, "credit"))
                .projectCount(AdminRows.asLongOrZero(row, "project_count"))
                .build();
    }

    private AdminUserDetailResponseDto toDetail(Tuple row) {
        return AdminUserDetailResponseDto.builder()
                .id(AdminRows.asLong(row, "id"))
                .email(AdminRows.asString(row, "email"))
                .name(AdminRows.asString(row, "name"))
                .role(AdminRows.asEnum(row, "role", UserRole.class))
                .registeredAt(AdminRows.asDateTime(row, "registered_at"))
                .credit(AdminRows.asInt(row, "credit"))
                .projectCount(AdminRows.asLongOrZero(row, "project_count"))
                .modifiedAt(AdminRows.asDateTime(row, "modified_at"))
                .creditEarned(AdminRows.asLongOrZero(row, "credit_earned"))
                .creditUsed(AdminRows.asLongOrZero(row, "credit_used"))
                .lastProjectAt(AdminRows.asDateTime(row, "last_project_at"))
                .build();
    }
}
