package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminPersonaResponseDto(
        Long id,
        String name,
        int age,
        Gender gender,
        AgeGroup ageGroup,
        String job,
        String mbti,
        String signatureQuote,
        List<String> tags,
        long usageCount,
        long opinionCount,
        Double averageScore,
        LocalDateTime lastUsedAt
) {
    public AdminPersonaResponseDto withTags(List<String> tags) {
        return new AdminPersonaResponseDto(id, name, age, gender, ageGroup, job, mbti, signatureQuote, tags,
                usageCount, opinionCount, averageScore, lastUsedAt);
    }
}
