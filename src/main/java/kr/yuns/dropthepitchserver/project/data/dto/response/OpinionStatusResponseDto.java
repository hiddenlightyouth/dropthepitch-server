package kr.yuns.dropthepitchserver.project.data.dto.response;

import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import lombok.Builder;

@Builder
public record OpinionStatusResponseDto(
        OpinionCollectionStatus status,
        String statusDisplay
) {
}
