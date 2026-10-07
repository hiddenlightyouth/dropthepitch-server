package kr.yuns.dropthepitchserver.admin.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class AdminSelfRoleChangeException extends GlobalException {
    public AdminSelfRoleChangeException() {
        super(ErrorCode.INVALID_REQUEST, "자신의 역할은 변경할 수 없습니다.");
    }
}
