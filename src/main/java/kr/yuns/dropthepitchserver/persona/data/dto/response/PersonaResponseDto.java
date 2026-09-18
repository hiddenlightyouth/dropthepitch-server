package kr.yuns.dropthepitchserver.persona.data.dto.response;

import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import lombok.Builder;

import java.util.List;

@Builder
public record PersonaResponseDto(
        Long personaId,
        String name,
        Integer age,
        Gender gender,
        @Deprecated
        String imageUrl,
        List<String> tags,

        String nationality,
        String education,
        String academicTrack,
        String residence,
        String job,
        String militaryService,
        String family,
        String relationshipStatus,

        String income,
        String assets,
        String consumptionHabit,
        String coreValue,

        String personality,
        String mbti,
        String belief,
        String riskTolerance,
        String directionPreference,
        String sharingTendency,

        String hobby,
        String exercise,
        String dayNightPattern,
        String overseasTravel,
        String attentionSpan,

        String mobileOs,
        String aiLiteracy,
        String newTechAttitude,
        String infoSource,

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
) {
}
