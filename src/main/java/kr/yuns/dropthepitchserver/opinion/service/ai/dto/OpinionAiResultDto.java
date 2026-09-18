package kr.yuns.dropthepitchserver.opinion.service.ai.dto;

import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionDetailType;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;

import java.util.List;
import java.util.stream.Stream;

public record OpinionAiResultDto(
        Sentiment attitude,
        Double score,
        String summary,
        List<String> positives,
        List<String> negatives,
        List<String> improvements
) {
    private static final double LOWER_MARGIN = 0.5;
    private static final double UPPER_MARGIN = 0.4;

    public Double adjustedScore() {
        if (attitude == null) {
            return score;
        }
        double center = attitude.getScore();
        double value = score == null ? center : score;
        return Math.clamp(value, center - LOWER_MARGIN, center + UPPER_MARGIN);
    }

    public List<Detail> toDetails() {
        return Stream.of(
                        toDetails(OpinionDetailType.POSITIVE, positives),
                        toDetails(OpinionDetailType.NEGATIVE, negatives),
                        toDetails(OpinionDetailType.IMPROVEMENT, improvements))
                .flatMap(List::stream)
                .toList();
    }

    private static List<Detail> toDetails(OpinionDetailType type, List<String> contents) {
        if (contents == null) {
            return List.of();
        }
        return contents.stream()
                .map(content -> new Detail(type, content))
                .toList();
    }

    public record Detail(
            OpinionDetailType type,
            String content
    ) {}
}
