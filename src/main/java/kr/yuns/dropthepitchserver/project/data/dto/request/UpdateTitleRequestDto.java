package kr.yuns.dropthepitchserver.project.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTitleRequestDto {
    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다.")
    private String title;
}
