package kr.yuns.dropthepitchserver.credit.data.enums;

import kr.yuns.dropthepitchserver.payment.data.enums.PaymentReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

//크레딧 내역 한 줄에 표시되는 구분
@Getter
@RequiredArgsConstructor
public enum CreditHistoryType {
    FILE_ANALYSIS("파일 분석", CreditHistoryCategory.USED),
    OPINION_COLLECTION("의견 수집", CreditHistoryCategory.USED),
    CHARGE("크레딧 충전", CreditHistoryCategory.PAID),
    REFUND("크레딧 환불", CreditHistoryCategory.EARNED), //분석 실패시 환불
    SIGNUP_BONUS("가입 지급", CreditHistoryCategory.EARNED),
    ADMIN_GRANT("관리자 지급", CreditHistoryCategory.EARNED);

    private final String displayName;
    private final CreditHistoryCategory category;

    /**
     * 충전 사유를 내역 구분으로 바꿉니다.
     * 결제 도메인의 값을 응답에 그대로 내보내지 않기 위해 한 번 거칩니다.
     */
    public static CreditHistoryType from(PaymentReason reason) {
        //PaymentReason에 값이 추가되면 여기서 컴파일 오류가 나므로 빠뜨릴 수 없다.
        return switch (reason) {
            case CHARGE -> CHARGE;
            case REFUND -> REFUND;
            case SIGNUP_BONUS -> SIGNUP_BONUS;
            case ADMIN_GRANT -> ADMIN_GRANT;
        };
    }
}
