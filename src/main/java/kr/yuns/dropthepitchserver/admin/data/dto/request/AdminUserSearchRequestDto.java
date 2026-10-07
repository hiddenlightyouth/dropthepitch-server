package kr.yuns.dropthepitchserver.admin.data.dto.request;

import kr.yuns.dropthepitchserver.user.data.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserSearchRequestDto extends AdminPageRequestDto {
    private String keyword;
    private UserRole role;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private Integer minCredit;
    private Integer maxCredit;

    private Boolean hasProject;
}
