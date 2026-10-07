package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminProjectSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectResponseDto;
import kr.yuns.dropthepitchserver.admin.data.enums.AdminProjectSort;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminProjectQueryRepository {
    private static final String USED_CREDIT = """
            ((select coalesce(sum(fc.use_credit), 0) from file_analysis_credit fc where fc.project_id = p.id)
              + (select coalesce(sum(oc.use_credit), 0) from opinion_request_credit oc where oc.project_id = p.id))""";

    private static final String AVERAGE_SCORE =
            "(select round(avg(o.score), 1) from opinion o where o.project_id = p.id)";

    private static final String SELECT = """
            select p.id as id,
                   p.title as title,
                   p.status as status,
                   p.opinion_collection_status as opinion_collection_status,
                   f.type as input_type,
                   p.user_id as user_id,
                   u.name as user_name,
                   u.email as user_email,
                   p.created_at as created_at,
                   p.updated_at as updated_at,
                   p.deleted_at as deleted_at,
                   %s as used_credit,
                   %s as average_score
            """.formatted(USED_CREDIT, AVERAGE_SCORE);

    private static final String FROM = """
             from project p
             join `user` u on u.id = p.user_id
             left join file f on f.project_id = p.id
             left join analysis a on a.project_id = p.id
            """;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminPageResponseDto<AdminProjectResponseDto> search(AdminProjectSearchRequestDto request) {
        AdminConditions conditions = new AdminConditions()
                .keyword(request.getKeyword(), "p.title", "coalesce(f.name, '')", "u.name", "u.email")
                .add(request.getStatus(), "p.status = :status", "status")
                .add(request.getOpinionStatus(), "p.opinion_collection_status = :opinionStatus", "opinionStatus")
                .add(request.getFileType(), "f.type = :fileType", "fileType")
                .add(request.getVerdict(), "a.reject_reason = :verdict", "verdict")
                .add(request.getUserId(), "p.user_id = :userId", "userId")
                .period(request.getFrom(), request.getTo(), "p.created_at")
                .range(request.getMinScore(), request.getMaxScore(), AVERAGE_SCORE, "score");

        if (request.isDeletedOnly()) {
            conditions.add("p.deleted_at is not null");
        } else if (!request.isDeletedIncluded()) {
            conditions.add("p.deleted_at is null");
        }

        long totalCount = AdminRows.number(entityManager,
                "select count(*)" + FROM + conditions.where(), conditions);

        AdminProjectSort sort = AdminProjectSort.from(request.getSort());
        String orderBy = " order by " + sort.orderBy(request.isAscending()) + ", p.id desc";

        List<AdminProjectResponseDto> items = AdminRows.page(entityManager,
                SELECT + FROM + conditions.where() + orderBy, conditions,
                request.getPage(), request.getSize(), this::toSummary);

        return AdminPageResponseDto.of(items, request.getPage(), request.getSize(), totalCount);
    }

    private AdminProjectResponseDto toSummary(Tuple row) {
        return AdminProjectResponseDto.builder()
                .id(AdminRows.asLong(row, "id"))
                .title(AdminRows.asString(row, "title"))
                .status(AdminRows.asEnum(row, "status", ProjectStatus.class))
                .opinionCollectionStatus(
                        AdminRows.asEnum(row, "opinion_collection_status", OpinionCollectionStatus.class))
                .inputType(AdminRows.asEnum(row, "input_type", InputType.class))
                .userId(AdminRows.asLong(row, "user_id"))
                .userName(AdminRows.asString(row, "user_name"))
                .userEmail(AdminRows.asString(row, "user_email"))
                .createdAt(AdminRows.asDateTime(row, "created_at"))
                .updatedAt(AdminRows.asDateTime(row, "updated_at"))
                .deletedAt(AdminRows.asDateTime(row, "deleted_at"))
                .usedCredit(AdminRows.asLongOrZero(row, "used_credit"))
                .averageScore(AdminRows.asScore(row, "average_score"))
                .build();
    }
}
