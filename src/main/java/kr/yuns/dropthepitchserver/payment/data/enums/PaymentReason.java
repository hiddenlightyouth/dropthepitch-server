package kr.yuns.dropthepitchserver.payment.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentReason {
    CHARGE("크레딧 충전"),
    REFUND("결제 환불"),
    SIGNUP_BONUS("가입 지급"),
    ADMIN_GRANT("관리자 지급");

    private final String displayName;
}
