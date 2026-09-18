package kr.yuns.dropthepitchserver.project.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class ProjectNotFoundException extends GlobalException {
    public ProjectNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}
