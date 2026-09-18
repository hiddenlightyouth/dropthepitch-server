package kr.yuns.dropthepitchserver.persona.data.dto.response;

import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import lombok.Builder;

import java.util.List;

//의견 수집 전에 보여주는 선정 페르소나 카드 Dto.
//이 시점에는 의견이 없으므로 감정, 요약, 상세 의견은 담지 않는다.
@Builder
public record SelectedPersonaResponseDto(
        Long personaId,
        String name,
        Integer age,
        Gender gender,
        @Deprecated
        String imageUrl,
        List<String> tags
) { }
