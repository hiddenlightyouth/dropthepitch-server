package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class AnalysisNotCompletedException extends GlobalException {
    public AnalysisNotCompletedException() {
        super(ErrorCode.ANALYZE_NOT_COMPLETED);
    }
}
