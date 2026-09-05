package kr.yuns.dropthepitchserver.opinion.service;

import kr.yuns.dropthepitchserver.opinion.data.dto.response.OpinionListResponseDto;
import kr.yuns.dropthepitchserver.opinion.data.dto.response.PersonaOpinionResponseDto;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionSortType;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionNotFoundException;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import kr.yuns.dropthepitchserver.persona.data.entity.PersonaTag;
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
public class OpinionService {
    private final OpinionRepository opinionRepository;

    /**
     * 의견 ID로 Opinion을 조회합니다.
     * 사용자가 생성한 프로젝트의 의견만 조회할 수 있습니다.
     *
     * @param opinionsId 의견 ID
     * @param email 사용자 이메일 주소
     * @return Opinion
     */
    private Opinion GET_OPINION_BY_ID_WITH_CHECK_PERMISSION(Long opinionsId, String email) {
        Optional<Opinion> opinion = opinionRepository.findByIdAndProject_User_Email(opinionsId, email);

        if(opinion.isPresent()) {
            return opinion.get();
        } else {
            throw new OpinionNotFoundException();
        }
    }

    /**
     * 페르소나의 프로필과 한 줄 의견을 조회합니다.
     *
     * @param email 사용자 이메일 주소
     * @param opinionsId 의견 ID
     * @return Persona profile, summary
     */
    @Transactional(readOnly = true)
    public PersonaOpinionResponseDto getPersonaOpinionSummary(String email, Long opinionsId) {
        Opinion opinion = GET_OPINION_BY_ID_WITH_CHECK_PERMISSION(opinionsId, email);
        Persona persona = opinion.getPersona();
        Sentiment sentiment = opinion.getSentiment();

        return PersonaOpinionResponseDto.builder()
                .personaId(persona.getId())
                .personaProfileUrl(persona.getImageUrl())
                .personaName(persona.getName())
                .personaAge(persona.getAge())
                .sentimentEnum(sentiment)
                .sentimentDisplay(sentiment == null ? null : sentiment.getDisplayName())
                .summary(opinion.getSummary())
                .build();
    }

    /**
     * 프로젝트의 의견 목록을 연령대 필터와 정렬 기준에 맞춰 조회합니다.
     * 사용자가 생성한 프로젝트의 의견만 조회할 수 있습니다.
     *
     * @param email 사용자 이메일 주소
     * @param projectId 프로젝트 ID
     * @param ageGroup 연령대, 없으면 전체 조회
     * @param sortType 정렬 기준, 없으면 평점 높은 순
     * @return 페르소나 정보, 태그, 상세 의견을 포함한 의견 목록
     */
    @Transactional(readOnly = true)
    public List<OpinionListResponseDto> getOpinionList(String email, Long projectId,
                                                       AgeGroup ageGroup, OpinionSortType sortType) {
        List<Opinion> opinions = opinionRepository.findAllByProjectIdWithPersonaAndTags(projectId, email);
        opinionRepository.findAllByProjectIdWithDetails(projectId);

        Comparator<Opinion> comparator = Comparator.comparingInt(opinion -> opinion.getSentiment().getScore());
        if (sortType != OpinionSortType.SCORE_ASC) {
            comparator = comparator.reversed();
        }

        log.info("[getOpinionList] 의견 목록 조회: projectId={}, ageGroup={}, sort={}", projectId, ageGroup, sortType);

        return opinions.stream()
                .filter(opinion -> opinion.getSentiment() != null)
                .filter(opinion -> ageGroup == null || AgeGroup.from(opinion.getPersona().getAge()) == ageGroup)
                .sorted(comparator)
                .map(this::toOpinionListResponse)
                .toList();
    }

    /**
     * Opinion을 의견 카드 응답으로 변환합니다.
     *
     * @param opinion Opinion
     * @return 페르소나 정보, 태그, 상세 의견
     */
    private OpinionListResponseDto toOpinionListResponse(Opinion opinion) {
        Persona persona = opinion.getPersona();
        Sentiment sentiment = opinion.getSentiment();

        return OpinionListResponseDto.builder()
                .opinionId(opinion.getId())
                .personaId(persona.getId())
                .personaName(persona.getName())
                .personaAge(persona.getAge())
                .personaGender(persona.getGender())
                .personaProfileUrl(persona.getImageUrl())
                .personaTags(persona.getPersonaTags().stream().map(PersonaTag::getName).toList())
                .sentiment(sentiment)
                .sentimentDisplay(sentiment.getDisplayName())
                .score(sentiment.getScore())
                .summary(opinion.getSummary())
                .details(opinion.getOpinionDetails().stream()
                        .map(detail -> OpinionListResponseDto.OpinionDetailResponseDto.builder()
                                .type(detail.getType())
                                .typeDisplay(detail.getType().getDisplayName())
                                .content(detail.getContent())
                                .build())
                        .toList())
                .build();
    }
}
