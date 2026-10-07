package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryCategory;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminCreditHistoryResponseDto(
        String id,
        CreditHistoryType type,
        CreditHistoryCategory category,
        int amount,
        Long userId,
        String userName,
        String userEmail,
        Long projectId,
        String projectTitle,
        boolean projectDeleted,
        LocalDateTime occurredAt
) { }
