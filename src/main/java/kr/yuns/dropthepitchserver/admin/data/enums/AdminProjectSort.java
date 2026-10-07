package kr.yuns.dropthepitchserver.admin.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AdminProjectSort {
    CREATED_AT("createdAt", "p.created_at", false),
    AVERAGE_SCORE("averageScore", "average_score", true),
    USED_CREDIT("usedCredit", "used_credit", false);

    private final String param;
    private final String column;
    private final boolean nullable;

    public String orderBy(boolean ascending) {
        String direction = ascending ? " asc" : " desc";
        return (nullable ? column + " is null, " : "") + column + direction;
    }

    public static AdminProjectSort from(String param) {
        return Arrays.stream(values())
                .filter(sort -> sort.param.equals(param))
                .findFirst()
                .orElse(CREATED_AT);
    }
}
