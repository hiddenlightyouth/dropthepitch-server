package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;
import lombok.Getter;

@Getter
public class FileAnalysisFailedException extends GlobalException {
    private final AnalysisVerdict reason;

    public FileAnalysisFailedException(AnalysisVerdict reason) {
        super(ErrorCode.FILE_ANALYSIS_FAILED, reason.getMessage());
        this.reason = reason;
    }
}
