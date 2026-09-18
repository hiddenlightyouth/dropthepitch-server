package kr.yuns.dropthepitchserver.opinion.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class OpinionNotFoundException extends GlobalException {
    public OpinionNotFoundException() {
        super(ErrorCode.OPINION_DATA_NOT_FOUND);
    }
}
