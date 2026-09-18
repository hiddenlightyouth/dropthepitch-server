package kr.yuns.dropthepitchserver.credit.data.dto.projection;

import java.time.LocalDateTime;

//크레딧을 쓴 내역 쿼리 결과 담기
public interface CreditUseView {
    Long getProjectId();

    String getProjectTitle();

    Integer getUseCredit();
    LocalDateTime getProjectDeletedAt();

    LocalDateTime getOccurredAt();
}
