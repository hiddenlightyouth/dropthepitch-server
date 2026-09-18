package kr.yuns.dropthepitchserver.credit.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class InvalidCreditAmountException extends GlobalException {
    public InvalidCreditAmountException() {
        super(ErrorCode.INVALID_CREDIT_AMOUNT);
    }
}
