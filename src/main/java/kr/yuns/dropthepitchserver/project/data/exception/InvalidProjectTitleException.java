package kr.yuns.dropthepitchserver.project.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class InvalidProjectTitleException extends GlobalException {
    public InvalidProjectTitleException() {
        super(ErrorCode.INVALID_PROJECT_TITLE);
    }
}
