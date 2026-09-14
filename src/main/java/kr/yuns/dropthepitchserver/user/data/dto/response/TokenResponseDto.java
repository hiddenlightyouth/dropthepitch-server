package kr.yuns.dropthepitchserver.user.data.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    private String name;
    private String accessToken;
    private String refreshToken;
}
