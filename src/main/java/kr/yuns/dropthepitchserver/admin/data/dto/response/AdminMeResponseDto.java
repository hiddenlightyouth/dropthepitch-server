package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.user.data.enums.UserRole;
import lombok.Builder;

@Builder
public record AdminMeResponseDto(
        String email,
        String name,
        UserRole role
) { }
