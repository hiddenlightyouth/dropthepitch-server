package kr.yuns.dropthepitchserver.admin.data.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.yuns.dropthepitchserver.user.data.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRoleChangeRequestDto {
    @NotNull
    private UserRole role;
}
