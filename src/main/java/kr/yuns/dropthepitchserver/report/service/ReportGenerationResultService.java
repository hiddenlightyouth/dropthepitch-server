package kr.yuns.dropthepitchserver.report.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.exception.AnalysisNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.entity.OpinionDetail;
import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionDetailType;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import kr.yuns.dropthepitchserver.report.data.dto.ai.ReportGenerationResult;
import kr.yuns.dropthepitchserver.report.data.dto.ai.ReportPromptInput;
import kr.yuns.dropthepitchserver.report.data.entity.Report;
import kr.yuns.dropthepitchserver.report.data.entity.ReportItem;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportItemType;
import kr.yuns.dropthepitchserver.report.data.enums.ReportStatus;
import kr.yuns.dropthepitchserver.report.data.exception.ReportNotFoundException;
import kr.yuns.dropthepitchserver.report.data.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationResultService {

    private final AnalysisRepository analysisRepository;
    private final OpinionRepository opinionRepository;
    private final ReportRepository reportRepository;

    //리포트 생성 유무 확인
    @Transactional(readOnly = true)
    public boolean isAlreadyGenerated(Long projectId) {
        return reportRepository.findByProjectId(projectId)
                .map(report -> report.getStatus() == ReportStatus.DONE)
                .orElse(false);
    }

    @Transactional
    public void saveReport(Long projectId, ReportGenerationResult result) {
        Report report = reportRepository.findByProjectId(projectId)
                .orElseThrow(ReportNotFoundException::new);

        //재확인
        if (report.getStatus() == ReportStatus.DONE) {
            log.warn("[saveReport] 이미 만들어진 리포트입니다: projectId={}", projectId);
            return;
        }

        report.completeReport(result.summary(), result.insight());

        addItems(report, ReportItemType.POSITIVE_POINT, null, result.positivePoints());
        addItems(report, ReportItemType.NEGATIVE_POINT, null, result.negativePoints());

        result.ageInsights().forEach(insight -> {
            addItem(report, ReportItemType.AGE_KEY_POINT, insight.ageGroup(), insight.keyPoint());
            addItem(report, ReportItemType.AGE_PAIN_POINT, insight.ageGroup(), insight.painPoint());
            addItem(report, ReportItemType.AGE_IMPROVEMENT, insight.ageGroup(), insight.improvement());
        });

        log.info("[saveReport] 리포트 저장: projectId={}, 항목={}건", projectId, report.getReportItems().size());
    }

    private void addItems(Report report, ReportItemType type, AgeGroup ageGroup, List<String> contents) {
        if (contents == null) {
            return;
        }
        contents.forEach(content -> addItem(report, type, ageGroup, content));
    }

    //내용이 빈 항목은 화면에 빈 줄로 남으므로 저장하지 않는다.
    private void addItem(Report report, ReportItemType type, AgeGroup ageGroup, String content) {
        if (!StringUtils.hasText(content)) {
            log.warn("[saveReport] 내용이 비어 건너뜁니다: type={}, ageGroup={}", type, ageGroup);
            return;
        }
        report.addReportItem(ReportItem.builder()
                .report(report)
                .type(type)
                .ageGroup(ageGroup)
                .content(content)
                .build());
    }

    //의견을 텍스트로 전환
    @Transactional(readOnly = true)
    public ReportPromptInput getPromptInput(Long projectId) {
        Analysis analysis = analysisRepository.findByProjectId(projectId)
                .orElseThrow(AnalysisNotFoundException::new);

        List<Opinion> opinions = opinionRepository.findAllByProjectIdWithPersona(projectId);

        opinionRepository.findAllByProjectIdWithDetails(projectId);

        List<Opinion> collected = opinions.stream()
                .filter(opinion -> opinion.getSentiment() != null)
                .toList();

        String brief = StringUtils.hasText(analysis.getDetail())
                ? analysis.getDetail()
                : analysis.getContent();

        log.info("[getPromptInput] 리포트 재료 조회: projectId={}, 수집된 의견={}건", projectId, collected.size());

        return new ReportPromptInput(brief, toOpinionText(collected), collected.size());
    }

    //연령대별로 나이순으로 정렬
    private String toOpinionText(List<Opinion> opinions) {
        return opinions.stream()
                .sorted(Comparator.comparingInt(opinion -> opinion.getPersona().getAge()))
                .map(this::toOpinionBlock)
                .collect(Collectors.joining("\n\n"));
    }

    private String toOpinionBlock(Opinion opinion) {
        Persona persona = opinion.getPersona();
        StringBuilder block = new StringBuilder();

        block.append("[")
                .append(AgeGroup.from(persona.getAge()).getDisplayName()).append(" · ")
                .append(persona.getGender()).append(" · ")
                .append(opinion.getSentiment().getDisplayName())
                .append("]");

        if (StringUtils.hasText(opinion.getSummary())) {
            block.append("\n한 줄: ").append(opinion.getSummary());
        }

        for (OpinionDetailType type : OpinionDetailType.values()) {
            opinion.getOpinionDetails().stream()
                    .filter(detail -> detail.getType() == type)
                    .map(OpinionDetail::getContent)
                    .filter(StringUtils::hasText)
                    .forEach(content -> block.append("\n").append(type.getDisplayName()).append(": ").append(content));
        }

        return block.toString();
    }
}
