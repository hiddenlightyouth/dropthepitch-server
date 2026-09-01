package kr.yuns.dropthepitchserver.opinion.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sentiment {
    VERY_NEGATIVE("매우 부정적", 1),
    NEGATIVE("부정적", 2),
    NEUTRAL("보통", 3),
    POSITIVE("긍정적", 4),
    VERY_POSITIVE("매우 긍정적", 5);

    private final String displayName;
    private final int score;
}
