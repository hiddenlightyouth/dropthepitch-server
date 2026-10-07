package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminCreditSummaryResponseDto(
        long issued,
        long used,
        long outstanding,
        List<TypeSummary> byType
) {
    public record TypeSummary(
            CreditHistoryType type,
            long count,
            long amount
    ) { }
}
