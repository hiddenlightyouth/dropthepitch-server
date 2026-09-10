package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.FileAnalysisResult;
import kr.yuns.dropthepitchserver.analyze.data.dto.response.FileAnalyzeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalysisDetailReader {

    private static final String TARGET_NOT_STATED = "자료에 명시되지 않음";
    private static final Set<String> CONSUMER_NOT_FOUND = Set.of("자료의 대상과 동일", "자료에서 추정 근거를 찾을 수 없음");

    private final ObjectMapper objectMapper;

    /**
     * 저장해 둔 분석 결과 JSON에서 화면에 보여줄 태그와 항목을 꺼냅니다.
     * 읽지 못하면 비어 있는 결과를 돌려줍니다.
     *
     * @param detailJson 분석 결과 JSON
     * @return 화면용 태그와 항목
     */
    public AnalysisDisplay read(String detailJson) {
        if (!StringUtils.hasText(detailJson)) {
            return AnalysisDisplay.EMPTY;
        }

        FileAnalysisResult result;

        try {
            result = objectMapper.readValue(detailJson, FileAnalysisResult.class);
        } catch (Exception e) {
            //스키마를 고치면 예전에 저장한 JSON이 새 타입과 맞지 않을 수 있다. 화면은 요약만으로도 뜬다.
            log.warn("[read] 분석 결과를 읽지 못했습니다.", e);
            return AnalysisDisplay.EMPTY;
        }

        FileAnalysisResult.AnalysisDetail detail = result.detail();
        List<FileAnalyzeResponseDto.AnalysisDetailResponseDto> details = new ArrayList<>();

        if (detail != null) {
            addDetail(details, "자료 종류", single(detail.category()));
            addDetail(details, "타깃", single(target(detail)));
            addDetail(details, "빠진 정보", items(detail.informationGaps()));
        }

        return new AnalysisDisplay(items(result.keywords()), details);
    }

    private static void addDetail(List<FileAnalyzeResponseDto.AnalysisDetailResponseDto> details, String label, List<String> contents) {
        if (!contents.isEmpty()) {
            details.add(FileAnalyzeResponseDto.AnalysisDetailResponseDto.builder()
                    .label(label)
                    .analyzeContents(contents)
                    .build());
        }
    }

    //자료가 타깃을 말하지 않았으면 자료에서 추정한 최종 사용자를 대신 보여준다.
    private static String target(FileAnalysisResult.AnalysisDetail detail) {
        String targetAudience = detail.targetAudience();
        String endConsumer = detail.endConsumer();

        boolean targetMissing = !StringUtils.hasText(targetAudience) || TARGET_NOT_STATED.equals(targetAudience.trim());
        boolean consumerUsable = StringUtils.hasText(endConsumer) && !CONSUMER_NOT_FOUND.contains(endConsumer.trim());

        return targetMissing && consumerUsable ? endConsumer : targetAudience;
    }

    private static List<String> single(String value) {
        return StringUtils.hasText(value) ? List.of(value) : List.of();
    }

    private static List<String> items(List<String> values) {
        return values == null ? List.of() : values.stream().filter(StringUtils::hasText).toList();
    }

    public record AnalysisDisplay(
            List<String> keywords,
            List<FileAnalyzeResponseDto.AnalysisDetailResponseDto> details
    ) {
        static final AnalysisDisplay EMPTY = new AnalysisDisplay(List.of(), List.of());
    }
}
