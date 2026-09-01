package kr.yuns.dropthepitchserver.analyze.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalysisStatus {
    IN_PROGRESS("분석중"),
    COMPLETED("분석완료"),
    FAILED("분석실패");

    private final String displayName;
}
