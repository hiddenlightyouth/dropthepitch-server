package kr.yuns.dropthepitchserver.common.s3.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class FileTooLargeException extends GlobalException {
    public FileTooLargeException() {
        super(ErrorCode.FILE_TOO_LARGE);
    }
}
