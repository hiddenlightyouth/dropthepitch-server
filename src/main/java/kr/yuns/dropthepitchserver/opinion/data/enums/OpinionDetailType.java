package kr.yuns.dropthepitchserver.opinion.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpinionDetailType {
    POSITIVE("긍정 평가"),
    NEGATIVE("부정 평가"),
    IMPROVEMENT("개선 요청");

    private final String displayName;
}
