package kr.yuns.dropthepitchserver.credit.data.dto.response;

import lombok.Builder;

//보유 크레딧 Dto
@Builder
public record CreditResponseDto(
        Integer amount
) { }
