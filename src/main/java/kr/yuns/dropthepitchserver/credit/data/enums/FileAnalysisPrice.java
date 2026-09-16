package kr.yuns.dropthepitchserver.credit.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum FileAnalysisPrice {
    DOCUMENT_IMAGE(0, 3),
    VIDEO_UNDER_3MIN(180, 6),
    VIDEO_UNDER_5MIN(300, 9),
    VIDEO_UNDER_10MIN(600, 17),
    VIDEO_UNDER_30MIN(1800, 46);

    public static final int MAX_VIDEO_SECONDS = 1800;

    private final int maxSeconds;
    private final int credit;

    /**
     * 자료에 따라 차감할 크레딧을 돌려줍니다.
     *
     * @param videoSeconds 영상 길이(초). 영상이 아니면 null
     * @return 차감할 크레딧
     */
    public static int of(Integer videoSeconds) {
        if (videoSeconds == null) {
            return DOCUMENT_IMAGE.credit;
        }

        return Arrays.stream(values())
                .filter(price -> price != DOCUMENT_IMAGE && videoSeconds <= price.maxSeconds)
                .findFirst()
                .orElse(VIDEO_UNDER_30MIN)
                .credit;
    }
}
