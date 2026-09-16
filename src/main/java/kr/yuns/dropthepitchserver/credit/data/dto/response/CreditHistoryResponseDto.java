package kr.yuns.dropthepitchserver.credit.data.dto.response;

import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryCategory;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import lombok.Builder;

import java.time.LocalDateTime;

//크레딧 내역 한 줄.
@Builder
public record CreditHistoryResponseDto(
        CreditHistoryType type,
        String typeDisplay,
        CreditHistoryCategory category,
        //사용은 음수, 충전과 적립은 양수
        Integer amount,
        //이 거래 직후의 잔액. 현재 잔액에서 거꾸로 계산한 값이
        Integer balance,
        //어떤 작업에 썼는지
        Long projectId,
        String projectTitle,
        //거래가 일어난 시각
        LocalDateTime occurredAt
) { }
