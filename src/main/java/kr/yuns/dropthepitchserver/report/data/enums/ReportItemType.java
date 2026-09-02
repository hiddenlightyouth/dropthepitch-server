package kr.yuns.dropthepitchserver.report.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportItemType {
    POSITIVE_POINT("긍정적인 포인트"),
    NEGATIVE_POINT("부정적인 포인트"),
    AGE_KEY_POINT("연령대별 핵심 포인트"),
    AGE_PAIN_POINT("연령대별 불편한 점"),
    AGE_IMPROVEMENT("연령대별 개선 요청");

    private final String displayName;
}
