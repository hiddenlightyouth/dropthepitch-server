package kr.yuns.dropthepitchserver.opinion.service;

import kr.yuns.dropthepitchserver.opinion.data.dto.response.PersonaOpinionResponseDto;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionNotFoundException;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
