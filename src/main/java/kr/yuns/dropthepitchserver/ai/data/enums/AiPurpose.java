package kr.yuns.dropthepitchserver.ai.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiPurpose {
    ANALYSIS("파일 분석"),
    OPINION("페르소나 의견 수집"),
    REPORT("리포트 생성");

    private final String displayName;
}
