package kr.yuns.dropthepitchserver.admin.data.dto.request;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminAiUsageSearchRequestDto extends AdminPageRequestDto {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private AiPurpose purpose;
    private String model;
    private InputType fileType;
    private String status;
    private String keyword;
}
