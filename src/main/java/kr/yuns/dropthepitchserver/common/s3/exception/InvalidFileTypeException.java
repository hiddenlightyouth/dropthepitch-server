package kr.yuns.dropthepitchserver.common.s3.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class InvalidFileTypeException extends GlobalException {
    public InvalidFileTypeException() {
        super(ErrorCode.INVALID_FILE_TYPE);
    }
}
