package kr.yuns.dropthepitchserver.report.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class ReportGenerationFailedException extends GlobalException {
    public ReportGenerationFailedException() {
        super(ErrorCode.REPORT_GENERATION_FAILED);
    }
}
