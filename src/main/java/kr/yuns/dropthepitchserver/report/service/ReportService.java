package kr.yuns.dropthepitchserver.report.service;

import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.report.data.dto.response.ReportSummaryResponseDto;
import kr.yuns.dropthepitchserver.report.data.entity.Report;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.exception.ReportNotFoundException;
import kr.yuns.dropthepitchserver.report.data.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final OpinionRepository opinionRepository;

    private Report getReportEntity(String email, String reportId) {
        return reportRepository.findByUuidAndProject_User_Email(reportId, email)
                .orElseThrow(() -> {
                    log.warn("[getReportEntity] 리포트 조회 실패: reportId={}, email={}", reportId, email);
                    return new ReportNotFoundException();
                });
    }

    //연령대별로 의견의 점수 평균, 수집된 의견 연령대가 없는 경우  null로 처리.
    private List<ReportSummaryResponseDto.AgeScoreResponseDto> calculateAgeScores(Long projectId) {
        Map<AgeGroup, Double> scores = opinionRepository.findAllByProjectIdWithPersona(projectId).stream()
                .filter(opinion -> opinion.getSentiment() != null)
                .collect(Collectors.groupingBy(
                        opinion -> AgeGroup.from(opinion.getPersona().getAge()),
                        Collectors.averagingInt(opinion -> opinion.getSentiment().getScore())));

        return Arrays.stream(AgeGroup.values())
                .map(ageGroup -> ReportSummaryResponseDto.AgeScoreResponseDto.builder()
                        .ageGroup(ageGroup)
                        .displayName(ageGroup.getDisplayName())
                        .score(scores.get(ageGroup))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponseDto getSummary(String email, String reportId) {
        Report report = getReportEntity(email, reportId);

        return ReportSummaryResponseDto.builder()
                .reportId(report.getUuid())
                .status(report.getStatus())
                .summary(report.getSummary())
                .insight(report.getInsight())
                .ageScores(calculateAgeScores(report.getProject().getId()))
                .build();
    }
}
