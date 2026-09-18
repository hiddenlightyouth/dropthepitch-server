package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class FileNotFoundException extends GlobalException {
    public FileNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}