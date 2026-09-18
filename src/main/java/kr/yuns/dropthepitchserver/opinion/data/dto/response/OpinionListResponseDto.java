package kr.yuns.dropthepitchserver.opinion.data.dto.response;

import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionDetailType;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import lombok.Builder;

import java.util.List;

//상세 리포트의 사용자 의견 카드 Dto
@Builder
public record OpinionListResponseDto(
        Long opinionId,
        Long personaId,
        String personaName,
        Integer personaAge,
        Gender personaGender,
        String personaProfileUrl,
        List<String> personaTags,
        Sentiment sentiment,
        String sentimentDisplay,
        Double score,
        String summary,
        List<OpinionDetailResponseDto> details
) {
    //긍정, 부정, 개선 상세 평가
    @Builder
    public record OpinionDetailResponseDto(
            OpinionDetailType type,
            String typeDisplay,
            String content
    ) { }
}
