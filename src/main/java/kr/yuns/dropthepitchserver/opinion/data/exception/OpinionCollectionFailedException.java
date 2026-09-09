package kr.yuns.dropthepitchserver.opinion.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class OpinionCollectionFailedException extends GlobalException {
    public OpinionCollectionFailedException() {
        super(ErrorCode.OPINION_COLLECTION_FAILED);
    }
}
