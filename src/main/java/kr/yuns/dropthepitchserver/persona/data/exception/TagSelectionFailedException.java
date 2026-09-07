package kr.yuns.dropthepitchserver.persona.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class TagSelectionFailedException extends GlobalException {
    public TagSelectionFailedException() {
        super(ErrorCode.TAG_SELECTION_FAILED);
    }
}
