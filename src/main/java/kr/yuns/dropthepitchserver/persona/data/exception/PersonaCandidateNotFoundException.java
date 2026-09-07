package kr.yuns.dropthepitchserver.persona.data.exception;

import kr.yuns.dropthepitchserver.common.response.ErrorCode;
import kr.yuns.dropthepitchserver.common.response.GlobalException;

public class PersonaCandidateNotFoundException extends GlobalException {
    public PersonaCandidateNotFoundException() {
        super(ErrorCode.PERSONA_CANDIDATE_NOT_FOUND);
    }
}
