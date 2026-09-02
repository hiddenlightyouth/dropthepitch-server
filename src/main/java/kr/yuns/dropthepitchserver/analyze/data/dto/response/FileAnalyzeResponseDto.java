package kr.yuns.dropthepitchserver.analyze.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisStatus;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import lombok.Builder;

import java.util.List;

@Builder
public record FileAnalyzeResponseDto(
        String uuid,
        AnalysisStatus analysisStatus,
        InputType type,
        String downloadUrl,
        String name,
        Integer size,
        String thumbnailUrl,
        String analyzeContent,
        List<VideoTimelineResponseDto> videoTimeline
) {
    @Builder
    public record VideoTimelineResponseDto(
            Integer startTime,
            Integer endTime,
            String analyzeContent
    ) {}
}