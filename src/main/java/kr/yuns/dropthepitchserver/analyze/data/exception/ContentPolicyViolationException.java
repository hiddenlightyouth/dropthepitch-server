package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class ContentPolicyViolationException extends GlobalException {
    public ContentPolicyViolationException() {
        super(ErrorCode.CONTENT_POLICY_VIOLATION);
    }
}
