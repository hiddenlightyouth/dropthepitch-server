package kr.yuns.dropthepitchserver.admin.data.dto.request;

import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryCategory;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCreditHistorySearchRequestDto extends AdminPageRequestDto {
    private String keyword;
    private CreditHistoryCategory category;
    private CreditHistoryType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private Integer minAmount;
    private Integer maxAmount;

    private Long userId;
}
