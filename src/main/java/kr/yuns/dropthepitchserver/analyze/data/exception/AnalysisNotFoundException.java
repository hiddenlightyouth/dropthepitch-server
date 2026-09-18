package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class AnalysisNotFoundException extends GlobalException {
    public AnalysisNotFoundException() {
        super(ErrorCode.ANALYZE_DATA_NOT_FOUND);
    }
}
