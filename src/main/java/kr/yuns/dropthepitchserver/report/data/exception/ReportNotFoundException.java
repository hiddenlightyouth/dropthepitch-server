package kr.yuns.dropthepitchserver.report.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class ReportNotFoundException extends GlobalException {
    public ReportNotFoundException() {
        super(ErrorCode.REPORT_DATA_NOT_FOUND);
    }
}
