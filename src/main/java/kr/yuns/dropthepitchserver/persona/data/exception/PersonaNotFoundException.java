package kr.yuns.dropthepitchserver.persona.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class PersonaNotFoundException extends GlobalException {
    public PersonaNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}
