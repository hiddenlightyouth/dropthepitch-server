package kr.yuns.dropthepitchserver.analyze.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class FileAnalysisFailedException extends GlobalException {
    public FileAnalysisFailedException() {
        super(ErrorCode.FILE_ANALYSIS_FAILED);
    }
}
