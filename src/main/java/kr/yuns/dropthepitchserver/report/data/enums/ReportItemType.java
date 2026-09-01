package kr.yuns.dropthepitchserver.report.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportItemType {
    OVERALL_SUMMARY("전체 요약"),
    POSITIVE_SUMMARY("긍정 요약"),
    NEGATIVE_SUMMARY("부정 요약"),
    AGE_SUMMARY("연령대별 요약");

    private final String displayName;
}
