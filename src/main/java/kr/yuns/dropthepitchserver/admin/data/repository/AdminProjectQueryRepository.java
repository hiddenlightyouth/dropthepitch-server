package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminProjectSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminAiPurposeUsageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto.AnalysisInfo;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto.FileInfo;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto.OpinionInfo;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto.ReportInfo;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto.ReportItemInfo;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectResponseDto;
import kr.yuns.dropthepitchserver.admin.data.enums.AdminProjectSort;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.admin.support.AiUsageSum;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisStatus;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportItemType;
import kr.yuns.dropthepitchserver.report.data.enums.ReportStatus;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public List<AdminProjectResponseDto> findRecent(int limit) {
        AdminConditions conditions = new AdminConditions().add("p.deleted_at is null");
        return AdminRows.page(entityManager,
                SELECT + FROM + conditions.where() + " order by p.created_at desc, p.id desc", conditions,
                0, limit, this::toSummary);
    }

    public Optional<AdminProjectResponseDto> findSummary(Long projectId) {
        AdminConditions conditions = new AdminConditions().add(projectId, "p.id = :projectId", "projectId");
        return AdminRows.list(entityManager, SELECT + FROM + conditions.where(), conditions, this::toSummary)
                .stream().findFirst();
    }

    public Optional<FileInfo> findFile(Long projectId) {
        AdminConditions conditions = byProject(projectId);
        String sql = "select f.name as name, f.type as type, f.size as size, f.url as url,"
                + " f.thumbnail_url as thumbnail_url from file f where f.project_id = :projectId";
        return AdminRows.list(entityManager, sql, conditions, row -> new FileInfo(
                AdminRows.asString(row, "name"),
                AdminRows.asEnum(row, "type", InputType.class),
                AdminRows.asLongOrZero(row, "size"),
                AdminRows.asString(row, "url"),
                AdminRows.asString(row, "thumbnail_url"))).stream().findFirst();
    }

    public Optional<AnalysisInfo> findAnalysis(Long projectId) {
        AdminConditions conditions = byProject(projectId);
        String sql = "select a.uuid as uuid, a.status as status, a.reject_reason as reject_reason,"
                + " a.content as content, a.selected_tags as selected_tags from analysis a where a.project_id = :projectId";
        return AdminRows.list(entityManager, sql, conditions, row -> new AnalysisInfo(
                AdminRows.asString(row, "uuid"),
                AdminRows.asEnum(row, "status", AnalysisStatus.class),
                AdminRows.asEnum(row, "reject_reason", AnalysisVerdict.class),
                AdminRows.asString(row, "content"),
                splitTags(AdminRows.asString(row, "selected_tags")))).stream().findFirst();
    }

    public List<OpinionInfo> findOpinions(Long projectId) {
        AdminConditions conditions = byProject(projectId);
        String sql = """
                select o.uuid as uuid, o.persona_id as persona_id, pe.name as persona_name, pe.age as persona_age,
                       pe.gender as persona_gender, o.score as score, o.sentiment as sentiment, o.summary as summary
                  from opinion o
                  join persona pe on pe.id = o.persona_id
                 where o.project_id = :projectId
                 order by pe.age, pe.id
                """;
        return AdminRows.list(entityManager, sql, conditions, row -> new OpinionInfo(
                AdminRows.asString(row, "uuid"),
                AdminRows.asLong(row, "persona_id"),
                AdminRows.asString(row, "persona_name"),
                AdminRows.asInt(row, "persona_age"),
                AdminRows.asEnum(row, "persona_gender", Gender.class),
                AdminRows.asDouble(row, "score"),
                AdminRows.asEnum(row, "sentiment", Sentiment.class),
                AdminRows.asString(row, "summary")));
    }

    public Optional<ReportInfo> findReport(Long projectId) {
        AdminConditions conditions = byProject(projectId);
        String sql = "select r.id as id, r.uuid as uuid, r.status as status, r.summary as summary,"
                + " r.insight as insight from report r where r.project_id = :projectId";
        return AdminRows.list(entityManager, sql, conditions, row -> new ReportInfo(
                AdminRows.asString(row, "uuid"),
                AdminRows.asEnum(row, "status", ReportStatus.class),
                AdminRows.asString(row, "summary"),
                AdminRows.asString(row, "insight"),
                findReportItems(AdminRows.asLong(row, "id")))).stream().findFirst();
    }

    private List<ReportItemInfo> findReportItems(Long reportId) {
        AdminConditions conditions = new AdminConditions().add(reportId, "i.report_id = :reportId", "reportId");
        String sql = "select i.type as type, i.age_group as age_group, i.content as content from report_item i"
                + conditions.where() + " order by i.id";
        return AdminRows.list(entityManager, sql, conditions, row -> new ReportItemInfo(
                AdminRows.asEnum(row, "type", ReportItemType.class),
                AdminRows.asEnum(row, "age_group", AgeGroup.class),
                AdminRows.asString(row, "content")));
    }

    public List<AdminAiPurposeUsageResponseDto> findAiUsage(Long projectId) {
        AdminConditions conditions = byProject(projectId);
        String sql = """
                select a.purpose as purpose, a.model as model, count(*) as calls,
                       sum(a.input_tokens) as input_tokens, sum(a.output_tokens) as output_tokens
                  from ai_usage a
                 where a.project_id = :projectId
                 group by a.purpose, a.model
                """;

        Map<AiPurpose, AiUsageSum> sums = new EnumMap<>(AiPurpose.class);
        AdminRows.list(entityManager, sql, conditions, row -> row).forEach(row ->
                sums.computeIfAbsent(AdminRows.asEnum(row, "purpose", AiPurpose.class), purpose -> new AiUsageSum())
                        .add(AdminRows.asString(row, "model"),
                                AdminRows.asLongOrZero(row, "calls"),
                                AdminRows.asLongOrZero(row, "input_tokens"),
                                AdminRows.asLongOrZero(row, "output_tokens")));

        return sums.entrySet().stream()
                .map(entry -> AdminAiPurposeUsageResponseDto.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(AdminAiPurposeUsageResponseDto::totalCost).reversed())
                .toList();
    }

    private AdminConditions byProject(Long projectId) {
        return new AdminConditions().param("projectId", projectId);
    }

    private List<String> splitTags(String selectedTags) {
        if (selectedTags == null || selectedTags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(selectedTags.split(",")).map(String::trim).filter(tag -> !tag.isEmpty()).toList();
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
