package kr.yuns.dropthepitchserver.admin.data.dto.request;

import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminPersonaSearchRequestDto extends AdminPageRequestDto {
    private String keyword;
    private AgeGroup ageGroup;
    private Gender gender;
    private String mbti;
    private String tag;
    private String usage;

    public boolean isUsedOnly() {
        return "USED".equals(usage);
    }

    public boolean isUnusedOnly() {
        return "UNUSED".equals(usage);
    }
}
