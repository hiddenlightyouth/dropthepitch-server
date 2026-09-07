package kr.yuns.dropthepitchserver.persona.service;

import kr.yuns.dropthepitchserver.persona.data.dto.response.PersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.data.entity.*;
import kr.yuns.dropthepitchserver.persona.data.exception.PersonaNotFoundException;
import kr.yuns.dropthepitchserver.persona.data.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaService {
    private final PersonaRepository personaRepository;

    /**
     * 페르소나 ID로 Persona를 가져옵니다.
     *
     * @param personaId 페르소나 ID
     * @return Persona
     */
    private Persona GET_PERSONA_BY_ID(Long personaId) {
        Optional<Persona> persona = personaRepository.findDetailById(personaId);

        if (persona.isPresent()) {
            return persona.get();
        } else {
            log.error("[GET_PERSONA_BY_ID] 페르소나 정보 조회 실패: {}", personaId);
            throw new PersonaNotFoundException();
        }
    }

    /**
     * 개별 페르소나의 상세 정보 조회합니다.
     *
     * @param personaId 페르소나 ID
     * @return 해당 페르소나 정보(PersonaResponseDto)
     */
    @Transactional(readOnly = true)
    public PersonaResponseDto getPersona(Long personaId) {
        Persona persona = GET_PERSONA_BY_ID(personaId);

        log.info("[getPersona] 페르소나 정보 조회: {}", personaId);

        return toResponseDto(persona);
    }

    /**
     * 모든 페르소나의 상세 정보를 조회합니다.
     *
     * @return 전체 페르소나 정보(PersonaResponseDto)
     */
    @Transactional(readOnly = true)
    public List<PersonaResponseDto> getAllPersonas() {
        List<Persona> personas = personaRepository.findAllDetail();

        log.info("[getAllPersonas] 전체 페르소나 정보 조회: {}건", personas.size());

        return personas.stream()
                .map(this::toResponseDto)
                .toList();
    }

    private PersonaResponseDto toResponseDto(Persona persona) {
        PersonaBasic basic = persona.getPersonaBasic();
        PersonaEconomy economy = persona.getPersonaEconomy();
        PersonaPsychology psychology = persona.getPersonaPsychology();
        PersonaLifestyle lifestyle = persona.getPersonaLifestyle();
        PersonaDigital digital = persona.getPersonaDigital();
        PersonaDecision decision = persona.getPersonaDecision();
        List<PersonaTag> personaTags = persona.getPersonaTags();

        return PersonaResponseDto.builder()
                .personaId(persona.getId())
                .name(persona.getName())
                .age(persona.getAge())
                .gender(persona.getGender())
                .imageUrl(persona.getImageUrl())
                .tags(personaTags.stream()
                        .map(PersonaTag::getName)
                        .toList())
                .nationality(basic.getNationality())
                .education(basic.getEducation())
                .academicTrack(basic.getAcademicTrack())
                .residence(basic.getResidence())
                .job(basic.getJob())
                .militaryService(basic.getMilitaryService())
                .family(basic.getFamily())
                .relationshipStatus(basic.getRelationshipStatus())
                .income(economy.getIncome())
                .assets(economy.getAssets())
                .consumptionHabit(economy.getConsumptionHabit())
                .coreValue(economy.getCoreValue())
                .personality(psychology.getPersonality())
                .mbti(psychology.getMbti())
                .belief(psychology.getBelief())
                .riskTolerance(psychology.getRiskTolerance())
                .directionPreference(psychology.getDirectionPreference())
                .sharingTendency(psychology.getSharingTendency())
                .hobby(lifestyle.getHobby())
                .exercise(lifestyle.getExercise())
                .dayNightPattern(lifestyle.getDayNightPattern())
                .overseasTravel(lifestyle.getOverseasTravel())
                .attentionSpan(lifestyle.getAttentionSpan())
                .mobileOs(digital.getMobileOs())
                .aiLiteracy(digital.getAiLiteracy())
                .newTechAttitude(digital.getNewTechAttitude())
                .infoSource(digital.getInfoSource())
                .biggestPain(decision.getBiggestPain())
                .churnTrigger(decision.getChurnTrigger())
                .signatureQuote(decision.getSignatureQuote())
                .communicationStyle(decision.getCommunicationStyle())
                .transportation(decision.getTransportation())
                .distrustPoint(decision.getDistrustPoint())
                .paymentTrigger(decision.getPaymentTrigger())
                .influencer(decision.getInfluencer())
                .community(decision.getCommunity())
                .healthStatus(decision.getHealthStatus())
                .build();
    }
}
