package kr.yuns.dropthepitchserver.admin.data.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminPersonaSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Usage;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaResponseDto;
import kr.yuns.dropthepitchserver.admin.data.enums.AdminPersonaSort;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminConditions;
import kr.yuns.dropthepitchserver.admin.data.repository.support.AdminRows;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AdminPersonaQueryRepository {
    private static final String TAG_NAMES =
            "coalesce((select group_concat(t.name) from persona_tag t where t.persona_id = p.id), '')";

    private static final String SELECT = """
            select p.id as id,
                   p.name as name,
                   p.age as age,
                   p.gender as gender,
                   b.job as job,
                   ps.mbti as mbti,
                   d.signature_quote as signature_quote,
                   coalesce(s.usage_count, 0) as usage_count,
                   coalesce(s.opinion_count, 0) as opinion_count,
                   s.average_score as average_score,
                   s.last_used_at as last_used_at
            """;

    private static final String FROM = """
             from persona p
             left join persona_basic b on b.persona_id = p.id
             left join persona_psychology ps on ps.persona_id = p.id
             left join persona_decision d on d.persona_id = p.id
             left join (select o.persona_id as persona_id,
                               count(*) as usage_count,
                               count(o.score) as opinion_count,
                               round(avg(o.score), 1) as average_score,
                               max(pr.created_at) as last_used_at
                          from opinion o
                          join project pr on pr.id = o.project_id
                         group by o.persona_id) s on s.persona_id = p.id
            """;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminPageResponseDto<AdminPersonaResponseDto> search(AdminPersonaSearchRequestDto request) {
        AdminConditions conditions = new AdminConditions()
                .keyword(request.getKeyword(), "p.name", "coalesce(b.job, '')",
                        "coalesce(d.signature_quote, '')", TAG_NAMES)
                .add(request.getGender(), "p.gender = :gender", "gender")
                .add(blankToNull(request.getMbti()), "ps.mbti = :mbti", "mbti");

        if (request.getAgeGroup() != null) {
            conditions.range(request.getAgeGroup().getStartAge(), request.getAgeGroup().getEndAge(), "p.age", "age");
        }

        String tag = blankToNull(request.getTag());
        if (tag != null) {
            String plain = tag.startsWith("#") ? tag.substring(1) : tag;
            conditions.add("exists (select 1 from persona_tag t where t.persona_id = p.id and t.name in (:tagNames))")
                    .param("tagNames", List.of(plain, "#" + plain));
        }

        if (request.isUsedOnly()) {
            conditions.add("s.persona_id is not null");
        } else if (request.isUnusedOnly()) {
            conditions.add("s.persona_id is null");
        }

        long totalCount = AdminRows.number(entityManager,
                "select count(*)" + FROM + conditions.where(), conditions);

        String orderBy = AdminPersonaSort.from(request.getSort())
                .map(sort -> " order by " + sort.orderBy(request.isAscending()) + ", p.id asc")
                .orElse(" order by p.id asc");

        List<AdminPersonaResponseDto> items = AdminRows.page(entityManager,
                SELECT + FROM + conditions.where() + orderBy, conditions,
                request.getPage(), request.getSize(), this::toSummary);

        return AdminPageResponseDto.of(attachTags(items), request.getPage(), request.getSize(), totalCount);
    }

    public Optional<AdminPersonaResponseDto> findSummary(Long personaId) {
        AdminConditions conditions = new AdminConditions().add(personaId, "p.id = :personaId", "personaId");
        List<AdminPersonaResponseDto> found = AdminRows.list(entityManager,
                SELECT + FROM + conditions.where(), conditions, this::toSummary);
        return attachTags(found).stream().findFirst();
    }

    public List<String> findTagNames() {
        return AdminRows.list(entityManager, "select distinct t.name as name from persona_tag t",
                        new AdminConditions(), row -> AdminRows.asString(row, "name"))
                .stream()
                .map(AdminPersonaQueryRepository::stripHash)
                .distinct()
                .sorted()
                .toList();
    }

    public List<Usage> findRecentUsages(Long personaId, int limit) {
        AdminConditions conditions = new AdminConditions().param("personaId", personaId);
        String sql = """
                select pr.id as project_id, pr.title as project_title, pr.deleted_at as deleted_at,
                       o.score as score, o.sentiment as sentiment, pr.created_at as used_at
                  from opinion o
                  join project pr on pr.id = o.project_id
                 where o.persona_id = :personaId
                 order by pr.created_at desc, pr.id desc
                """;
        return AdminRows.page(entityManager, sql, conditions, 0, limit, row -> new Usage(
                AdminRows.asLong(row, "project_id"),
                AdminRows.asString(row, "project_title"),
                AdminRows.asDateTime(row, "deleted_at") != null,
                AdminRows.asDouble(row, "score"),
                AdminRows.asEnum(row, "sentiment", Sentiment.class),
                AdminRows.asDateTime(row, "used_at")));
    }

    private List<AdminPersonaResponseDto> attachTags(List<AdminPersonaResponseDto> personas) {
        if (personas.isEmpty()) {
            return personas;
        }

        AdminConditions conditions = new AdminConditions()
                .in(personas.stream().map(AdminPersonaResponseDto::id).toList(), "t.persona_id", "personaIds");
        String sql = "select t.persona_id as persona_id, t.name as name from persona_tag t"
                + conditions.where() + " order by t.id";

        Map<Long, List<String>> tags = new HashMap<>();
        AdminRows.list(entityManager, sql, conditions, row -> row).forEach(row ->
                tags.computeIfAbsent(AdminRows.asLong(row, "persona_id"), id -> new ArrayList<>())
                        .add(stripHash(AdminRows.asString(row, "name"))));

        return personas.stream()
                .map(persona -> persona.withTags(tags.getOrDefault(persona.id(), List.of())))
                .toList();
    }

    private static String stripHash(String tag) {
        return tag != null && tag.startsWith("#") ? tag.substring(1) : tag;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static AgeGroup ageGroupOf(int age) {
        return Arrays.stream(AgeGroup.values())
                .filter(group -> age >= group.getStartAge() && age <= group.getEndAge())
                .findFirst()
                .orElse(null);
    }

    private AdminPersonaResponseDto toSummary(Tuple row) {
        int age = AdminRows.asInt(row, "age");
        return AdminPersonaResponseDto.builder()
                .id(AdminRows.asLong(row, "id"))
                .name(AdminRows.asString(row, "name"))
                .age(age)
                .gender(AdminRows.asEnum(row, "gender", Gender.class))
                .ageGroup(ageGroupOf(age))
                .job(AdminRows.asString(row, "job"))
                .mbti(AdminRows.asString(row, "mbti"))
                .signatureQuote(AdminRows.asString(row, "signature_quote"))
                .tags(List.of())
                .usageCount(AdminRows.asLongOrZero(row, "usage_count"))
                .opinionCount(AdminRows.asLongOrZero(row, "opinion_count"))
                .averageScore(AdminRows.asScore(row, "average_score"))
                .lastUsedAt(AdminRows.asDateTime(row, "last_used_at"))
                .build();
    }
}
