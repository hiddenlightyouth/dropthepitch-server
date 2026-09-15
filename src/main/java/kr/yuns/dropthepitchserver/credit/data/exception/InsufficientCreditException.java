package kr.yuns.dropthepitchserver.credit.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class InsufficientCreditException extends GlobalException {
    public InsufficientCreditException() {
        super(ErrorCode.INSUFFICIENT_CREDIT);
    }
}
