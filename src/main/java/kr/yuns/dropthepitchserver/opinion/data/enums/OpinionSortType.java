package kr.yuns.dropthepitchserver.opinion.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpinionSortType {
    SCORE_DESC("평점 높은 순"),
    SCORE_ASC("평점 낮은 순");

    private final String displayName;
}
