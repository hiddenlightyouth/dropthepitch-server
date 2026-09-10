package kr.yuns.dropthepitchserver.opinion.data.dto.response;

import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import lombok.Builder;

@Builder
public record PersonaOpinionResponseDto(
    Long personaId,
    String personaProfileUrl,
    String personaName,
    Integer personaAge,
    Sentiment sentimentEnum,
    String sentimentDisplay,
    Double score,
    String summary
) {}
