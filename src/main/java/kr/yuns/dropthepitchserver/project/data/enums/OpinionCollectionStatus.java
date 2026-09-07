package kr.yuns.dropthepitchserver.project.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpinionCollectionStatus {
    NOT_STARTED("수집 전"),
    IN_PROGRESS("수집 중"),
    COMPLETED("수집 완료"),
    FAILED("수집 실패");

    private final String displayName;
}
