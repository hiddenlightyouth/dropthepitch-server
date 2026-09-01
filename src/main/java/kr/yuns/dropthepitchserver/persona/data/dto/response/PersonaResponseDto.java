package kr.yuns.dropthepitchserver.persona.data.dto.response;

import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponseDto {
    private Long personaId;
    private String name;
    private Integer age;
    private Gender gender;
    private String imageUrl;
    private List<String> tags;

    private String nationality;
    private String education;
    private String academicTrack;
    private String residence;
    private String job;
    private String militaryService;
    private String family;
    private String relationshipStatus;

    private String income;
    private String assets;
    private String consumptionHabit;
    private String coreValue;

    private String personality;
    private String mbti;
    private String belief;
    private String riskTolerance;
    private String directionPreference;
    private String sharingTendency;

    private String hobby;
    private String exercise;
    private String dayNightPattern;
    private String overseasTravel;
    private String attentionSpan;

    private String mobileOs;
    private String aiLiteracy;
    private String newTechAttitude;
    private String infoSource;

    private String biggestPain;
    private String churnTrigger;
    private String signatureQuote;
    private String communicationStyle;
    private String transportation;
    private String distrustPoint;
    private String paymentTrigger;
    private String influencer;
    private String community;
    private String healthStatus;
}
