package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class AnalysisRejectedException extends GlobalException {
    public AnalysisRejectedException(AnalysisVerdict verdict) {
        super(ErrorCode.ANALYSIS_REJECTED, verdict.getMessage());
    }
}
