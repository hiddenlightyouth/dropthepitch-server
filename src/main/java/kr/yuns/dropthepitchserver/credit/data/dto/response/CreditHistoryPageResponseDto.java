package kr.yuns.dropthepitchserver.credit.data.dto.response;

import lombok.Builder;

import java.util.List;

//크레딧 내역 한 페이지.
//PC는 totalPages로 번호를 그리고, 모바일은 hasNext로 더 부를지 판단.
@Builder
public record CreditHistoryPageResponseDto(
        List<CreditHistoryResponseDto> items,
        int page,
        int size,
        int totalCount,
        int totalPages,
        boolean hasNext
) { }
