package kr.yuns.dropthepitchserver.report.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("생성대기"),
    DONE("생성완료");

    private final String displayName;
}
