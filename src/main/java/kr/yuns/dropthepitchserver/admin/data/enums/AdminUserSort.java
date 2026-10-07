package kr.yuns.dropthepitchserver.admin.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AdminUserSort {
    REGISTERED_AT("registeredAt", "u.registered_at"),
    CREDIT("credit", "credit"),
    PROJECT_COUNT("projectCount", "project_count"),
    NAME("name", "u.name");

    private final String param;
    private final String column;

    public static AdminUserSort from(String param) {
        return Arrays.stream(values())
                .filter(sort -> sort.param.equals(param))
                .findFirst()
                .orElse(REGISTERED_AT);
    }
}
