package kr.yuns.dropthepitchserver.opinion.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class OpinionAlreadyCollectedException extends GlobalException {
    public OpinionAlreadyCollectedException() {
        super(ErrorCode.OPINION_ALREADY_COLLECTED);
    }
}
