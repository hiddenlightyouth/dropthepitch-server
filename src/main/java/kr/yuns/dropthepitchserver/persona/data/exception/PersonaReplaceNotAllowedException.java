package kr.yuns.dropthepitchserver.persona.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class PersonaReplaceNotAllowedException extends GlobalException {
    public PersonaReplaceNotAllowedException() {
        super(ErrorCode.PERSONA_REPLACE_NOT_ALLOWED);
    }
}
