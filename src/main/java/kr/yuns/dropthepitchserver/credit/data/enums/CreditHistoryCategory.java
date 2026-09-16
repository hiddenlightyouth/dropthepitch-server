package kr.yuns.dropthepitchserver.credit.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//크레딧 내역 화면의 필터 탭
@Getter
@RequiredArgsConstructor
public enum CreditHistoryCategory {
    USED("사용"),
    PAID("충전"),
    EARNED("적립");

    private final String displayName;
}
