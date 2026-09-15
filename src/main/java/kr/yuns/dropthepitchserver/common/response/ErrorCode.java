package kr.yuns.dropthepitchserver.common.response;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_REQUEST("C001", "잘못된 요청입니다.", 400),
    INVALID_PARAMETER("C002", "유효하지 않은 파라미터입니다.", 400),
    INVALID_FILE_TYPE("C4002", "지원하지 않는 파일 형식입니다.", 400),
    ANALYZE_NOT_COMPLETED("C4003", "파일 분석이 아직 완료되지 않았습니다.", 400),
    INVALID_PROJECT_TITLE("C4004", "제목은 6자 이상 255자 이하여야 합니다.", 400),
    ANALYSIS_REJECTED("C4005", "분석할 수 없는 자료입니다.", 400),
    FILE_TOO_LARGE("C4006", "파일 크기는 100MB를 넘을 수 없습니다.", 400),
    INVALID_CREDIT_AMOUNT("C4007", "크레딧 변경 수량은 1 이상이어야 합니다.", 400),
    VERIFICATION_INVALID("C4011", "인증 정보가 유효하지 않습니다.", 401),
    PASSWORD_INVALID("C4012", "비밀번호가 일치하지 않습니다.", 401),
    TOKEN_INVALID("C4013", "유효하지 않은 토큰입니다.", 401),
    ACCESS_DENIED("C403", "승인되지 않은 사용자입니다.", 403),
    USER_DATA_NOT_FOUND("C4041", "사용자를 찾을 수 없습니다.", 404),
    DATA_NOT_FOUND("C404", "정보를 불러올 수 없습니다.", 404),
    ANALYZE_DATA_NOT_FOUND("C4042", "파일 분석 결과를 불러올 수 없거나 조회할 수 없는 분석 결과입니다.", 404),
    OPINION_DATA_NOT_FOUND("C4043", "의견 분석 결과를 불러올 수 없거나 조회할 수 없는 분석 결과입니다.", 404),
    REPORT_DATA_NOT_FOUND("C4044", "리포트를 불러올 수 없거나 조회할 수 없는 리포트입니다.", 404),
    EMAIL_DUPLICATION("C4091", "이미 존재하는 이메일입니다.", 409),
    OPINION_ALREADY_COLLECTED("C4092", "이미 의견 수집을 요청한 프로젝트입니다.", 409),
    PERSONA_REPLACE_NOT_ALLOWED("C4093", "의견 수집이 시작되어 페르소나를 교체할 수 없습니다.", 409),
    PERSONA_CANDIDATE_NOT_FOUND("C4094", "더 이상 교체할 페르소나가 없습니다.", 409),
    INSUFFICIENT_CREDIT("C4095", "크레딧이 부족합니다.", 409),
    FILE_UPLOAD_FAILED("C5001", "파일 업로드에 실패했습니다.", 500),
    FILE_ANALYSIS_FAILED("C5002", "파일 분석에 실패했습니다.", 500),
    FILE_DOWNLOAD_FAILED("C5003", "파일을 불러오지 못했습니다.", 500),
    TAG_SELECTION_FAILED("C5004", "페르소나 태그 선별에 실패했습니다.", 500),
    OPINION_COLLECTION_FAILED("C5005", "페르소나 의견 수집에 실패했습니다.", 500),
    REPORT_GENERATION_FAILED("C5006", "리포트 생성에 실패했습니다.", 500),
    UNKNOWN_ERROR("C500", "오류가 발생하였습니다.", 500);

    private final String code;
    private final String message;
    private final int httpStatus;

    ErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}