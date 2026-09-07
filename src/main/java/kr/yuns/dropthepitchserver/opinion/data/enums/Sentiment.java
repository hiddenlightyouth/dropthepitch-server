package kr.yuns.dropthepitchserver.opinion.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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

    //평균 점수 반올림으로 계산해서 가까운 감정 등급으로 환산 .
    public static Sentiment from(double score) {
        int rounded = (int) Math.round(Math.clamp(score, VERY_NEGATIVE.score, VERY_POSITIVE.score));
        return Arrays.stream(values())
                .filter(sentiment -> sentiment.score == rounded)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 점수입니다: " + score));
    }
}
