package kr.yuns.dropthepitchserver.user.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class EmailDuplicationException extends GlobalException {
    public EmailDuplicationException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }
}
