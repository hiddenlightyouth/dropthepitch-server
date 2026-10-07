package kr.yuns.dropthepitchserver.admin.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AdminCreditSort {
    OCCURRED_AT("occurredAt", "h.occurred_at"),
    AMOUNT("amount", "abs(h.amount)");

    private final String param;
    private final String column;

    public static AdminCreditSort from(String param) {
        return Arrays.stream(values())
                .filter(sort -> sort.param.equals(param))
                .findFirst()
                .orElse(OCCURRED_AT);
    }
}
