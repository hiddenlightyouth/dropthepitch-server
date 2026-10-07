package kr.yuns.dropthepitchserver.admin.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum AdminPersonaSort {
    USAGE_COUNT("usageCount", "coalesce(s.usage_count, 0)", false),
    AVERAGE_SCORE("averageScore", "s.average_score", true),
    LAST_USED_AT("lastUsedAt", "s.last_used_at", true),
    AGE("age", "p.age", false),
    NAME("name", "p.name", false);

    private final String param;
    private final String column;
    private final boolean nullable;

    public String orderBy(boolean ascending) {
        String direction = ascending ? " asc" : " desc";
        return (nullable ? column + " is null, " : "") + column + direction;
    }

    public static Optional<AdminPersonaSort> from(String param) {
        return Arrays.stream(values())
                .filter(sort -> sort.param.equals(param))
                .findFirst();
    }
}
