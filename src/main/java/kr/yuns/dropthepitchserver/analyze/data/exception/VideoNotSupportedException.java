package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class VideoNotSupportedException extends GlobalException {
    public VideoNotSupportedException(String detail) {
        super(ErrorCode.VIDEO_NOT_SUPPORTED, detail);
    }
}
