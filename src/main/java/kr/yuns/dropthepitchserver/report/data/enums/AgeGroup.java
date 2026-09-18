package kr.yuns.dropthepitchserver.report.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    TEENS("10대", 10, 19),
    TWENTIES("20대", 20, 29),
    THIRTIES("30대", 30, 39),
    FORTIES("40대", 40, 49),
    FIFTIES("50대", 50, 59),
    SIXTIES("60대", 60, 69);

    private final String displayName;
    private final int startAge;
    private final int endAge;

    //지원하지 않은 연령대(10~69)일떄 예외 처리.
    public static AgeGroup from(int age) {
        return Arrays.stream(values())
                .filter(ageGroup -> age >= ageGroup.startAge && age <= ageGroup.endAge)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 연령입니다: " + age));
    }
}
