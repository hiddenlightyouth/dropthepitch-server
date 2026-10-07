package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminPersonaSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Basic;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Decision;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Digital;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Economy;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Lifestyle;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto.Psychology;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminPersonaQueryRepository;
import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaBasic;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaDecision;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaDigital;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaEconomy;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaLifestyle;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaPsychology;
import kr.yuns.dropthepitchserver.persona.data.exception.PersonaNotFoundException;
import kr.yuns.dropthepitchserver.persona.data.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminPersonaService {
    private static final int RECENT_USAGE_LIMIT = 10;

    private final AdminPersonaQueryRepository adminPersonaQueryRepository;
    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminPersonaResponseDto> getPersonas(AdminPersonaSearchRequestDto request) {
        AdminPageResponseDto<AdminPersonaResponseDto> page = adminPersonaQueryRepository.search(request);
        log.info("[getPersonas] 페르소나 목록 조회: 전체 {}명, page={}", page.totalCount(), page.page());
        return page;
    }

    @Transactional(readOnly = true)
    public List<String> getTags() {
        return adminPersonaQueryRepository.findTagNames();
    }

    @Transactional(readOnly = true)
    public AdminPersonaDetailResponseDto getPersona(Long personaId) {
        AdminPersonaResponseDto summary = adminPersonaQueryRepository.findSummary(personaId)
                .orElseThrow(() -> {
                    log.warn("[getPersona] 페르소나 조회 실패: personaId={}", personaId);
                    return new PersonaNotFoundException();
                });
        Persona persona = personaRepository.findDetailById(personaId).orElseThrow(PersonaNotFoundException::new);

        return AdminPersonaDetailResponseDto.builder()
                .id(summary.id())
                .name(summary.name())
                .age(summary.age())
                .gender(summary.gender())
                .ageGroup(summary.ageGroup())
                .job(summary.job())
                .mbti(summary.mbti())
                .signatureQuote(summary.signatureQuote())
                .tags(summary.tags())
                .usageCount(summary.usageCount())
                .opinionCount(summary.opinionCount())
                .averageScore(summary.averageScore())
                .lastUsedAt(summary.lastUsedAt())
                .recentUsages(adminPersonaQueryRepository.findRecentUsages(personaId, RECENT_USAGE_LIMIT))
                .basic(toBasic(persona.getPersonaBasic()))
                .economy(toEconomy(persona.getPersonaEconomy()))
                .psychology(toPsychology(persona.getPersonaPsychology()))
                .lifestyle(toLifestyle(persona.getPersonaLifestyle()))
                .digital(toDigital(persona.getPersonaDigital()))
                .decision(toDecision(persona.getPersonaDecision()))
                .build();
    }

    private Basic toBasic(PersonaBasic basic) {
        if (basic == null) {
            return null;
        }
        return new Basic(basic.getNationality(), basic.getEducation(), basic.getAcademicTrack(),
                basic.getResidence(), basic.getJob(), basic.getMilitaryService(), basic.getFamily(),
                basic.getRelationshipStatus());
    }

    private Economy toEconomy(PersonaEconomy economy) {
        if (economy == null) {
            return null;
        }
        return new Economy(economy.getIncome(), economy.getAssets(), economy.getConsumptionHabit(),
                economy.getCoreValue());
    }

    private Psychology toPsychology(PersonaPsychology psychology) {
        if (psychology == null) {
            return null;
        }
        return new Psychology(psychology.getPersonality(), psychology.getMbti(), psychology.getBelief(),
                psychology.getRiskTolerance(), psychology.getDirectionPreference(),
                psychology.getSharingTendency());
    }

    private Lifestyle toLifestyle(PersonaLifestyle lifestyle) {
        if (lifestyle == null) {
            return null;
        }
        return new Lifestyle(lifestyle.getHobby(), lifestyle.getExercise(), lifestyle.getDayNightPattern(),
                lifestyle.getOverseasTravel(), lifestyle.getAttentionSpan());
    }

    private Digital toDigital(PersonaDigital digital) {
        if (digital == null) {
            return null;
        }
        return new Digital(digital.getMobileOs(), digital.getAiLiteracy(), digital.getNewTechAttitude(),
                digital.getInfoSource());
    }

    private Decision toDecision(PersonaDecision decision) {
        if (decision == null) {
            return null;
        }
        return new Decision(decision.getBiggestPain(), decision.getChurnTrigger(), decision.getSignatureQuote(),
                decision.getCommunicationStyle(), decision.getTransportation(), decision.getDistrustPoint(),
                decision.getPaymentTrigger(), decision.getInfluencer(), decision.getCommunity(),
                decision.getHealthStatus());
    }
}
