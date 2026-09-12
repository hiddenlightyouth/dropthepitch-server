package kr.yuns.dropthepitchserver.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GlobalErrorResponse {
    private final String code;
    private final String message;

    public static GlobalErrorResponse from(ErrorCode errorCode) {
        return GlobalErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static GlobalErrorResponse from(ErrorCode errorCode, String message) {
        return GlobalErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message == null ? errorCode.getMessage() : message)
                .build();
    }

    public static GlobalErrorResponse empty() {
        return new GlobalErrorResponse(null, null);
    }
}