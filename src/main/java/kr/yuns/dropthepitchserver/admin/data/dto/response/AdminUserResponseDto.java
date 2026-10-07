package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.user.data.enums.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminUserResponseDto(
        Long id,
        String email,
        String name,
        UserRole role,
        LocalDateTime registeredAt,
        int credit,
        long projectCount
) { }
