package kr.yuns.dropthepitchserver.common.response;

import lombok.Getter;

@Getter
public abstract class GlobalException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public GlobalException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public GlobalException(ErrorCode errorCode, String detail) {
        super(detail == null ? errorCode.getMessage() : detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}