package kr.yuns.dropthepitchserver.opinion.service;

import kr.yuns.dropthepitchserver.analyze.service.AnalyzeService;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.credit.data.entity.OpinionRequestCredit;
import kr.yuns.dropthepitchserver.credit.data.exception.InsufficientCreditException;
import kr.yuns.dropthepitchserver.credit.data.repository.CreditRepository;
import kr.yuns.dropthepitchserver.credit.data.repository.OpinionRequestCreditRepository;
import kr.yuns.dropthepitchserver.opinion.data.dto.projection.OpinionCollectionTarget;
import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionAlreadyCollectedException;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.opinion.service.ai.OpinionBriefFilter;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.exception.ProjectNotFoundException;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpinionCollectionService {
    private final OpinionCollectionRunner opinionCollectionRunner;
    private final ProjectRepository projectRepository;
    private final OpinionRepository opinionRepository;
    private final AnalyzeService analyzeService;
    private final OpinionBriefFilter opinionBriefFilter;
    private final CreditRepository creditRepository;
    private final OpinionRequestCreditRepository opinionRequestCreditRepository;
    private final TransactionTemplate transactionTemplate;

    // 의견 수집 및 리포트 생성 사용되는 크레딧
    private final static Integer OPINION_REPORT_CREDIT = 26;

    /**
     * 프로젝트에 속한 모든 페르소나의 의견 수집을 시작합니다.
     * 검증과 크레딧 차감은 동기로 처리하고 실제 수집은 백그라운드에서 진행됩니다.
     *
     * @param email 사용자 이메일 주소
     * @param projectId 프로젝트 ID
     * @return 수집을 시작한 페르소나 수
     */
    public int collectAllOpinions(String email, Long projectId) {
        Project project = getProject(email, projectId);
        String analysisBrief = opinionBriefFilter.filter(analyzeService.getCompletedAnalysisBrief(projectId));

        List<OpinionCollectionTarget> targets = transactionTemplate.execute(status ->
                startCollection(email, project));

        if (targets.isEmpty()) {
            log.warn("[collectAllOpinions] 수집할 페르소나가 없습니다: projectId={}", projectId);
            return 0;
        }

        opinionCollectionRunner.run(projectId, targets, analysisBrief);

        log.info("[collectAllOpinions] 의견 수집 요청 접수: projectId={}, 대상 {}건", projectId, targets.size());

        return targets.size();
    }

    private List<OpinionCollectionTarget> startCollection(String email, Project project) {
        Long projectId = project.getId();

        if (!projectRepository.startOpinionCollection(projectId)) {
            log.warn("[startCollection] 이미 수집을 요청한 프로젝트입니다: projectId={}, 상태={}",
                    projectId, project.getOpinionCollectionStatus());
            throw new OpinionAlreadyCollectedException();
        }

        List<OpinionCollectionTarget> targets = opinionRepository.findCollectionTargets(projectId);

        if (targets.isEmpty()) {
            projectRepository.finishOpinionCollection(projectId, OpinionCollectionStatus.COMPLETED);
            return targets;
        }

        useCredit(email, project);

        return targets;
    }

    private void useCredit(String email, Project project) {
        Credit credit = creditRepository.findWithLockByUserEmail(email)
                .orElseThrow(() -> {
                    log.warn("[useCredit] 크레딧 정보가 없습니다: projectId={}, email={}", project.getId(), email);
                    return new InsufficientCreditException();
                });

        OpinionRequestCredit opinionRequestCredit = OpinionRequestCredit.builder().build();
        opinionRequestCredit.use(credit, project, OPINION_REPORT_CREDIT);
        opinionRequestCreditRepository.save(opinionRequestCredit);

        log.info("[useCredit] 의견 수집 크레딧 차감: projectId={}, 차감 {}, 잔액 {}",
                project.getId(), OPINION_REPORT_CREDIT, credit.getAmount());
    }

    private Project getProject(String email, Long projectId) {
        return projectRepository.findByIdAndUserEmail(projectId, email)
                .orElseThrow(() -> {
                    log.warn("[getProject] 프로젝트 조회 실패: projectId={}, email={}", projectId, email);
                    return new ProjectNotFoundException();
                });
    }
}
