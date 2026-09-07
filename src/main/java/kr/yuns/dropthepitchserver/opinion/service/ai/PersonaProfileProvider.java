package kr.yuns.dropthepitchserver.opinion.service.ai;

import kr.yuns.dropthepitchserver.common.redis.RedisService;
import kr.yuns.dropthepitchserver.persona.data.dto.response.PersonaResponseDto;
import kr.yuns.dropthepitchserver.persona.service.PersonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PersonaProfileProvider {
    private static final String PROFILE_KEY = "persona:profile";

    private final PersonaService personaService;
    private final RedisService redisService;

    /**
     * 애플리케이션 기동 시 모든 페르소나 프로필을 렌더링해 Redis 에 적재합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadProfiles() {
        Map<String, String> profiles = personaService.getAllPersonas().stream()
                .collect(Collectors.toMap(
                        persona -> String.valueOf(persona.personaId()),
                        this::render,
                        (first, second) -> second));

        if (profiles.isEmpty()) {
            log.warn("[loadProfiles] 적재할 페르소나가 없습니다.");
            return;
        }

        redisService.setHashValues(PROFILE_KEY, profiles);

        log.info("[loadProfiles] 페르소나 프로필 적재 완료: {}건", profiles.size());
    }

    /**
     * 페르소나 프로필을 조회합니다.
     * 적재 이후 추가된 페르소나는 조회 시점에 렌더링해 함께 적재합니다.
     *
     * @param personaId 페르소나 ID
     * @return 프롬프트에 사용할 페르소나 프로필
     */
    public String getProfile(Long personaId) {
        String loaded = redisService.getHashValue(PROFILE_KEY, String.valueOf(personaId));

        if (loaded != null) {
            return loaded;
        }

        log.info("[getProfile] 적재되지 않은 페르소나입니다. 조회 시점에 렌더링합니다: {}", personaId);

        String profile = render(personaService.getPersona(personaId));
        redisService.setHashValue(PROFILE_KEY, String.valueOf(personaId), profile);

        return profile;
    }

    private String render(PersonaResponseDto persona) {
        StringBuilder sb = new StringBuilder("[페르소나]").append(System.lineSeparator());

        appendSection(sb, "기본");
        appendLine(sb, "이름", persona.name());
        appendLine(sb, "나이", persona.age());
        appendLine(sb, "성별", persona.gender() == null ? null : persona.gender().getDisplayName());
        appendLine(sb, "한 줄 소개", persona.signatureQuote());
        appendLine(sb, "키워드", persona.tags() == null || persona.tags().isEmpty() ? null : String.join(", ", persona.tags()));
        appendLine(sb, "국적", persona.nationality());
        appendLine(sb, "학력", persona.education());
        appendLine(sb, "계열", persona.academicTrack());
        appendLine(sb, "거주", persona.residence());
        appendLine(sb, "직업", persona.job());
        appendLine(sb, "병역", persona.militaryService());
        appendLine(sb, "가족", persona.family());
        appendLine(sb, "연애", persona.relationshipStatus());
        appendLine(sb, "건강", persona.healthStatus());

        appendSection(sb, "경제");
        appendLine(sb, "소득 수준", persona.income());
        appendLine(sb, "자산", persona.assets());
        appendLine(sb, "소비 습관", persona.consumptionHabit());
        appendLine(sb, "최우선 가치", persona.coreValue());
        appendLine(sb, "유료 전환 기준", persona.paymentTrigger());

        appendSection(sb, "성향");
        appendLine(sb, "성격", persona.personality());
        appendLine(sb, "MBTI", persona.mbti());
        appendLine(sb, "가치관", persona.belief());
        appendLine(sb, "위험 감수", persona.riskTolerance());
        appendLine(sb, "정치 성향", persona.directionPreference());
        appendLine(sb, "공유 성향", persona.sharingTendency());
        appendLine(sb, "말하는 방식", persona.communicationStyle());

        appendSection(sb, "생활");
        appendLine(sb, "취미", persona.hobby());
        appendLine(sb, "운동", persona.exercise());
        appendLine(sb, "생활 패턴", persona.dayNightPattern());
        appendLine(sb, "해외 경험", persona.overseasTravel());
        appendLine(sb, "집중 지속 시간", persona.attentionSpan());
        appendLine(sb, "이동 수단", persona.transportation());

        appendSection(sb, "디지털");
        appendLine(sb, "사용 기기", persona.mobileOs());
        appendLine(sb, "AI 이해도", persona.aiLiteracy());
        appendLine(sb, "신기술 반응", persona.newTechAttitude());
        appendLine(sb, "정보 습득 경로", persona.infoSource());
        appendLine(sb, "활동 커뮤니티", persona.community());

        appendSection(sb, "판단 기준");
        appendLine(sb, "가장 큰 불편", persona.biggestPain());
        appendLine(sb, "불신하는 지점", persona.distrustPoint());
        appendLine(sb, "이탈 트리거", persona.churnTrigger());
        appendLine(sb, "영향을 주는 사람", persona.influencer());

        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String title) {
        sb.append(System.lineSeparator()).append("<").append(title).append(">").append(System.lineSeparator());
    }

    private void appendLine(StringBuilder sb, String label, Object value) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            return;
        }
        sb.append("- ").append(label).append(": ").append(value).append(System.lineSeparator());
    }
}
