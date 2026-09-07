package kr.yuns.dropthepitchserver.common.s3.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class FileDownloadFailedException extends GlobalException {
    public FileDownloadFailedException() {
        super(ErrorCode.FILE_DOWNLOAD_FAILED);
    }
}
