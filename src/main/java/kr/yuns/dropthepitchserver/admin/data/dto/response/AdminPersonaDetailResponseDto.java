package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminPersonaDetailResponseDto(
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
        LocalDateTime lastUsedAt,
        List<Usage> recentUsages,
        Basic basic,
        Economy economy,
        Psychology psychology,
        Lifestyle lifestyle,
        Digital digital,
        Decision decision
) {
    public record Usage(
            Long projectId,
            String projectTitle,
            boolean projectDeleted,
            Double score,
            Sentiment sentiment,
            LocalDateTime usedAt
    ) { }

    public record Basic(
            String nationality,
            String education,
            String academicTrack,
            String residence,
            String job,
            String militaryService,
            String family,
            String relationshipStatus
    ) { }

    public record Economy(
            String income,
            String assets,
            String consumptionHabit,
            String coreValue
    ) { }

    public record Psychology(
            String personality,
            String mbti,
            String belief,
            String riskTolerance,
            String directionPreference,
            String sharingTendency
    ) { }

    public record Lifestyle(
            String hobby,
            String exercise,
            String dayNightPattern,
            String overseasTravel,
            String attentionSpan
    ) { }

    public record Digital(
            String mobileOs,
            String aiLiteracy,
            String newTechAttitude,
            String infoSource
    ) { }

    public record Decision(
            String biggestPain,
            String churnTrigger,
            String signatureQuote,
            String communicationStyle,
            String transportation,
            String distrustPoint,
            String paymentTrigger,
            String influencer,
            String community,
            String healthStatus
    ) { }
}
