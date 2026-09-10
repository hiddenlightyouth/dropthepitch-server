package kr.yuns.dropthepitchserver.opinion.service;

import kr.yuns.dropthepitchserver.analyze.service.AnalyzeService;
import kr.yuns.dropthepitchserver.opinion.data.dto.projection.OpinionCollectionTarget;
import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionAlreadyCollectedException;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.exception.ProjectNotFoundException;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpinionCollectionService {
    private final OpinionCollectionRunner opinionCollectionRunner;
    private final ProjectRepository projectRepository;
    private final OpinionRepository opinionRepository;
    private final AnalyzeService analyzeService;

    /**
     * 프로젝트에 속한 모든 페르소나의 의견 수집을 시작합니다.
     * 검증만 동기로 처리하고 실제 수집은 백그라운드에서 진행됩니다.
     *
     * @param email 사용자 이메일 주소
     * @param projectId 프로젝트 ID
     * @return 수집을 시작한 페르소나 수
     */
    public int collectAllOpinions(String email, Long projectId) {
        Project project = getProject(email, projectId);
        String analysisBrief = analyzeService.getCompletedAnalysisContent(projectId);

        if (!projectRepository.startOpinionCollection(projectId)) {
            log.warn("[collectAllOpinions] 이미 수집을 요청한 프로젝트입니다: projectId={}, 상태={}",
                    projectId, project.getOpinionCollectionStatus());
            throw new OpinionAlreadyCollectedException();
        }

        List<OpinionCollectionTarget> targets = opinionRepository.findCollectionTargets(projectId);

        if (targets.isEmpty()) {
            projectRepository.finishOpinionCollection(projectId, OpinionCollectionStatus.COMPLETED);
            log.warn("[collectAllOpinions] 수집할 페르소나가 없습니다: projectId={}", projectId);
            return 0;
        }

        opinionCollectionRunner.run(projectId, targets, analysisBrief);

        log.info("[collectAllOpinions] 의견 수집 요청 접수: projectId={}, 대상 {}건", projectId, targets.size());

        return targets.size();
    }

    private Project getProject(String email, Long projectId) {
        return projectRepository.findByIdAndUserEmail(projectId, email)
                .orElseThrow(() -> {
                    log.warn("[getProject] 프로젝트 조회 실패: projectId={}, email={}", projectId, email);
                    return new ProjectNotFoundException();
                });
    }
}
