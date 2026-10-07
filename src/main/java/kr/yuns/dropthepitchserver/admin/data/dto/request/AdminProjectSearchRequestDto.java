package kr.yuns.dropthepitchserver.admin.data.dto.request;

import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminProjectSearchRequestDto extends AdminPageRequestDto {
    private String keyword;
    private ProjectStatus status;
    private OpinionCollectionStatus opinionStatus;
    private InputType fileType;
    private AnalysisVerdict verdict;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private Double minScore;
    private Double maxScore;

    private String deleted;

    private Long userId;

    public boolean isDeletedIncluded() {
        return "INCLUDE".equals(deleted);
    }

    public boolean isDeletedOnly() {
        return "ONLY".equals(deleted);
    }
}
