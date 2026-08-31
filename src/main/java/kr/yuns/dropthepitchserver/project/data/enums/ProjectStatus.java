package kr.yuns.dropthepitchserver.project.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//일단은 진행 , 완료 , 실패
@Getter
@RequiredArgsConstructor
public enum ProjectStatus {
    IN_PROGRESS("진행"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String displayName;
}
