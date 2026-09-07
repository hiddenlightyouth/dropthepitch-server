package kr.yuns.dropthepitchserver.persona.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.persona.data.dto.response.PersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.data.dto.response.SelectedPersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.data.entity.*;
import kr.yuns.dropthepitchserver.persona.data.exception.PersonaCandidateNotFoundException;
import kr.yuns.dropthepitchserver.persona.data.exception.PersonaNotFoundException;
import kr.yuns.dropthepitchserver.persona.data.exception.PersonaReplaceNotAllowedException;
import kr.yuns.dropthepitchserver.persona.data.repository.PersonaRepository;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaService {
    private static final List<String> NO_TAG = List.of("");
    private final PersonaRepository personaRepository;
    //선정된 페르소나는 opinion 행으로 남아 있어 그쪽에서 읽는다.
    private final OpinionRepository opinionRepository;
    //교체할 때 선별에 썼던 태그를 다시 쓰기 위해 필요하다.
    private final AnalysisRepository analysisRepository;

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

        PersonaBasic basic = persona.getPersonaBasic();
        PersonaEconomy economy = persona.getPersonaEconomy();
        PersonaPsychology psychology = persona.getPersonaPsychology();
        PersonaLifestyle lifestyle = persona.getPersonaLifestyle();
        PersonaDigital digital = persona.getPersonaDigital();
        PersonaDecision decision = persona.getPersonaDecision();
        List<PersonaTag> personaTags = persona.getPersonaTags();

        log.info("[getPersona] 페르소나 정보 조회: {}", personaId);

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

    /**
     * 프로젝트에 선정된 페르소나 목록을 조회.
     * 의견 수집 전 화면에서 쓰이며, 아직 선별이 끝나지 않았으면 빈 목록
     */
    @Transactional(readOnly = true)
    public List<SelectedPersonaResponseDto> getSelectedPersonas(String email, Long projectId) {
        List<Opinion> opinions = opinionRepository.findAllByProjectIdWithPersonaAndTags(projectId, email);

        log.info("[getSelectedPersonas] 선정 페르소나 조회: projectId={}, {}명", projectId, opinions.size());

        return opinions.stream()
                .map(Opinion::getPersona)
                .sorted(Comparator.comparingInt(Persona::getAge))
                .map(this::toSelectedPersona)
                .toList();
    }

    /**
     * 선정된 페르소나 한 명을 같은 연령대의 다른 페르소나로 교체.
     * 선별에 썼던 태그를 재사용
     */
    @Transactional
    public SelectedPersonaResponseDto replacePersona(String email, Long projectId, Long personaId) {
        Opinion opinion = opinionRepository
                .findByProject_IdAndPersona_IdAndProject_User_Email(projectId, personaId, email)
                .orElseThrow(() -> {
                    log.warn("[replacePersona] 교체 대상 없음: projectId={}, personaId={}", projectId, personaId);
                    return new PersonaNotFoundException();
                });

        //의견 수집후 교체 예외처리
        if (opinionRepository.existsByProject_IdAndSentimentIsNotNull(projectId)) {
            log.warn("[replacePersona] 수집이 시작되어 교체 불가: projectId={}", projectId);
            throw new PersonaReplaceNotAllowedException();
        }


        AgeGroup ageGroup = AgeGroup.from(opinion.getPersona().getAge());
        List<String> tags = analysisRepository.findByProjectId(projectId)
                .map(Analysis::getSelectedTagList)
                .orElseGet(List::of);

        Long nextPersonaId = personaRepository.findTopMatchedIds(
                        tags.isEmpty() ? NO_TAG : tags,
                        ageGroup.getStartAge(), ageGroup.getEndAge(),
                        opinionRepository.findPersonaIdsByProjectId(projectId), 1)
                .stream()
                .findFirst()
                .orElseThrow(PersonaCandidateNotFoundException::new);

        //카드에 태그를 실어 보내야 해서 연관관계까지 함께 읽는다.
        Persona nextPersona = personaRepository.findDetailById(nextPersonaId)
                .orElseThrow(PersonaNotFoundException::new);
        opinion.replacePersona(nextPersona);

        log.info("[replacePersona] 페르소나 교체: projectId={}, {} -> {}", projectId, personaId, nextPersonaId);

        return toSelectedPersona(nextPersona);
    }

    private SelectedPersonaResponseDto toSelectedPersona(Persona persona) {
        return SelectedPersonaResponseDto.builder()
                .personaId(persona.getId())
                .name(persona.getName())
                .age(persona.getAge())
                .gender(persona.getGender())
                .imageUrl(persona.getImageUrl())
                .tags(persona.getPersonaTags().stream()
                        .map(PersonaTag::getName)
                        .toList())
                .build();
    }
}
