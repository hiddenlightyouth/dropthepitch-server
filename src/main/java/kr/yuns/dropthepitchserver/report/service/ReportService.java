package kr.yuns.dropthepitchserver.report.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.report.data.dto.response.ReportDetailResponseDto;
import kr.yuns.dropthepitchserver.report.data.dto.response.ReportSummaryResponseDto;
import kr.yuns.dropthepitchserver.report.data.entity.Report;
import kr.yuns.dropthepitchserver.report.data.entity.ReportItem;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportItemType;
import kr.yuns.dropthepitchserver.report.data.exception.ReportNotFoundException;
import kr.yuns.dropthepitchserver.report.data.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final OpinionRepository opinionRepository;
    private final FileRepository fileRepository;
    //전체 적으로 테이블 ERD를 좀 더 다듬어서 나중에 리팩터링 필요가 보임..
    //의견 집계 결과를 담음(순회 방지)
    private record ReportMetrics(
            Map<AgeGroup, Double> ageScores,
            long participantCount,
            Double averageScore
    ) { }

    private Report getReportEntity(String email, String reportId) {
        return reportRepository.findByUuidAndProject_User_Email(reportId, email)
                .orElseThrow(() -> {
                    log.warn("[getReportEntity] 리포트 조회 실패: reportId={}, email={}", reportId, email);
                    return new ReportNotFoundException();
                });
    }

    //화면 표기에 맞춰 소수점 한 자리로 자릅니다.
    private Double roundScore(Double score) {
        return score == null ? null : Math.round(score * 10) / 10.0;
    }

    //아직 채우지 않은 의견은 점수 계산에서 제외.
    private ReportMetrics calculateMetrics(List<Opinion> opinions) {
        List<Opinion> scored = opinions.stream()
                .filter(opinion -> opinion.getScore() != null)
                .toList();

        Map<AgeGroup, Double> ageScores = scored.stream()
                .collect(Collectors.groupingBy(
                        opinion -> AgeGroup.from(opinion.getPersona().getAge()),
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(Opinion::getScore),
                                this::roundScore)));

        OptionalDouble average = scored.stream()
                .mapToDouble(Opinion::getScore)
                .average();

        return new ReportMetrics(ageScores, opinions.size(),
                roundScore(average.isPresent() ? average.getAsDouble() : null));
    }

    //연령대 구분이 없는 전체 포인트를 항목별로 모읍니다.
    private List<String> extractPoints(Report report, ReportItemType type) {
        return report.getReportItems().stream()
                .filter(item -> item.getAgeGroup() == null && item.getType() == type)
                .map(ReportItem::getContent)
                .toList();
    }

    //연령대별 항목을 카드에 바로 꽂을 수 있도록 연령대 > 항목 구조 //하나의 테이블을 구조를 가지고 있어서 이부분 나중에 리팩터링 해야할듯...
    private List<ReportDetailResponseDto.AgeInsightResponseDto> buildAgeInsights(
            Report report, Map<AgeGroup, Double> ageScores) {
        Map<AgeGroup, Map<ReportItemType, String>> items = report.getReportItems().stream()
                .filter(item -> item.getAgeGroup() != null)
                .collect(Collectors.groupingBy(
                        ReportItem::getAgeGroup,
                        Collectors.toMap(ReportItem::getType, ReportItem::getContent)));

        return Arrays.stream(AgeGroup.values())
                .map(ageGroup -> {
                    Map<ReportItemType, String> item = items.getOrDefault(ageGroup, Map.of());
                    Double score = ageScores.get(ageGroup);

                    return ReportDetailResponseDto.AgeInsightResponseDto.builder()
                            .ageGroup(ageGroup)
                            .displayName(ageGroup.getDisplayName())
                            .score(score)
                            .sentiment(score == null ? null : Sentiment.from(score))
                            .keyPoint(item.get(ReportItemType.AGE_KEY_POINT))
                            .painPoint(item.get(ReportItemType.AGE_PAIN_POINT))
                            .improvement(item.get(ReportItemType.AGE_IMPROVEMENT))
                            .build();
                })
                .toList();
    }

    private ReportDetailResponseDto.StatsResponseDto buildStats(Long projectId, ReportMetrics metrics) {
        return ReportDetailResponseDto.StatsResponseDto.builder()
                .ageGroupCount(AgeGroup.values().length)
                .participantCount(metrics.participantCount())
                .sourceType(fileRepository.findByProjectId(projectId).map(File::getType).orElse(null))
                .averageScore(metrics.averageScore())
                .build();
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponseDto getSummary(String email, String reportId) {
        Report report = getReportEntity(email, reportId);
        ReportMetrics metrics = calculateMetrics(
                opinionRepository.findAllByProjectIdWithPersona(report.getProject().getId()));

        List<ReportSummaryResponseDto.AgeScoreResponseDto> ageScores = Arrays.stream(AgeGroup.values())
                .map(ageGroup -> ReportSummaryResponseDto.AgeScoreResponseDto.builder()
                        .ageGroup(ageGroup)
                        .displayName(ageGroup.getDisplayName())
                        .score(metrics.ageScores().get(ageGroup))
                        .build())
                .toList();

        return ReportSummaryResponseDto.builder()
                .reportId(report.getUuid())
                .status(report.getStatus())
                .summary(report.getSummary())
                .insight(report.getInsight())
                .ageScores(ageScores)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportDetailResponseDto getDetail(String email, String reportId) {
        Report report = getReportEntity(email, reportId);
        Long projectId = report.getProject().getId();
        ReportMetrics metrics = calculateMetrics(opinionRepository.findAllByProjectIdWithPersona(projectId));

        return ReportDetailResponseDto.builder()
                .reportId(report.getUuid())
                .status(report.getStatus())
                .summary(report.getSummary())
                .stats(buildStats(projectId, metrics))
                .positivePoints(extractPoints(report, ReportItemType.POSITIVE_POINT))
                .negativePoints(extractPoints(report, ReportItemType.NEGATIVE_POINT))
                .ageInsights(buildAgeInsights(report, metrics.ageScores()))
                .build();
    }
}
