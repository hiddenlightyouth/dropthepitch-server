package kr.yuns.dropthepitchserver.persona.service;

import kr.yuns.dropthepitchserver.ai.data.entity.AiUsage;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.ai.data.repository.AiUsageRepository;
import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.exception.AnalysisNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.persona.data.dto.ai.TagSelectionCallResult;
import kr.yuns.dropthepitchserver.persona.data.repository.PersonaRepository;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

//DB 작업
@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaMatchingResultService {

    private static final int PERSONA_PER_AGE_GROUP = 5;

    private static final List<Long> NOTHING_EXCLUDED = List.of(0L);

    //쓸 수 있는 태그가 하나도 없을 때
    private static final List<String> NO_TAG = List.of("");

    private final AnalysisRepository analysisRepository;
    private final PersonaRepository personaRepository;
    private final OpinionRepository opinionRepository;
    private final AiUsageRepository aiUsageRepository;

    @Transactional(readOnly = true)
    public String getAnalysisBrief(Long projectId) {
        Analysis analysis = getAnalysis(projectId);
        //상세가 비어있으면 한 줄 요약이라도 보는걸로 처리
        return StringUtils.hasText(analysis.getDetail()) ? analysis.getDetail() : analysis.getContent();
    }

     //선별 태그를 남김, 연령대마다 태그가 가장 많이 겹치는 페르소나를 뽑아 의견 자리 생성 ,callResult 사용량 기록에 필요한 AI 호출 결과
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSelection(Long projectId, List<String> tags, TagSelectionCallResult callResult) {
        Analysis analysis = getAnalysis(projectId);
        analysis.saveSelectedTags(tags);

        Project project = analysis.getProject();
        List<String> matchTags = tags.isEmpty() ? NO_TAG : tags;
        List<Opinion> opinions = new ArrayList<>();

        for (AgeGroup ageGroup : AgeGroup.values()) {
            List<Long> personaIds = personaRepository.findTopMatchedIds(
                    matchTags, ageGroup.getStartAge(), ageGroup.getEndAge(),
                    NOTHING_EXCLUDED, PERSONA_PER_AGE_GROUP);

            if (personaIds.size() < PERSONA_PER_AGE_GROUP) {
                log.warn("[saveSelection] 연령대 후보 부족: projectId={}, ageGroup={}, 선정={}명",
                        projectId, ageGroup, personaIds.size());
            }

            //getReferenceById는 조회 없이 프록시만 만듬.
            personaIds.forEach(personaId -> opinions.add(Opinion.builder()
                    .project(project)
                    .persona(personaRepository.getReferenceById(personaId))
                    .build()));
        }

        opinionRepository.saveAll(opinions);

        aiUsageRepository.save(AiUsage.builder()
                .project(project)
                .model(callResult.model())
                .purpose(AiPurpose.PERSONA_MATCHING)
                .inputTokens(callResult.inputTokens())
                .outputTokens(callResult.outputTokens())
                .build());

        log.info("[saveSelection] 선별 저장: projectId={}, 태그={}개, 페르소나={}명",
                projectId, tags.size(), opinions.size());
    }

    private Analysis getAnalysis(Long projectId) {
        return analysisRepository.findByProjectId(projectId)
                .orElseThrow(AnalysisNotFoundException::new);
    }
}
