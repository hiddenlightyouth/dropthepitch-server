package kr.yuns.dropthepitchserver.opinion.service.ai.dto;

import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionDetailType;

import java.util.List;

public record OpinionAiResultDto(
        Double score,
        String summary,
        List<Detail> details
) {
    public record Detail(
            OpinionDetailType type,
            String content
    ) {}
}
