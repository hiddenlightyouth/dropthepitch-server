package kr.yuns.dropthepitchserver.opinion.service;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.ai.service.AiService;
import kr.yuns.dropthepitchserver.ai.service.dto.SaveAiUsageCommand;
import kr.yuns.dropthepitchserver.opinion.config.OpinionAsyncConfiguration;
import kr.yuns.dropthepitchserver.opinion.data.dto.projection.OpinionCollectionTarget;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.entity.OpinionDetail;
import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionNotFoundException;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.opinion.event.OpinionCollectionCompletedEvent;
import kr.yuns.dropthepitchserver.opinion.service.ai.OpinionAiClient;
import kr.yuns.dropthepitchserver.opinion.service.ai.dto.OpinionAiResultDto;
import kr.yuns.dropthepitchserver.opinion.service.ai.dto.OpinionCallResult;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpinionCollectionRunner {
    private final OpinionAiClient opinionAiClient;
    private final OpinionRepository opinionRepository;
    private final TransactionTemplate transactionTemplate;
    private final ProjectRepository projectRepository;
    private final AiService aiService;
    private final ApplicationEventPublisher eventPublisher;

    @Qualifier(OpinionAsyncConfiguration.OPINION_COLLECTION_EXECUTOR)
    private final Executor opinionCollectionExecutor;

    private static final long CALL_TIMEOUT_SECONDS = 420L;

    /**
     * 클라이언트가 호출하면 즉시 응답하고, 백그라운드에서 페르소나 의견을 수집합니다.
     *
     * @param projectId 프로젝트 ID
     * @param targets 수집 대상 의견/페르소나 목록
     * @param analysisBrief 자료 분석 브리프
     */
    public void run(Long projectId, List<OpinionCollectionTarget> targets, String analysisBrief) {
        long startedAt = System.currentTimeMillis();
        log.info("[run] 의견 수집 시작: projectId={}, 대상 {}건", projectId, targets.size());

        List<CompletableFuture<Boolean>> futures = targets.stream()
                .map(target -> collectOne(projectId, target, analysisBrief))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .whenComplete((ignored, throwable) -> finish(projectId, futures, startedAt));
    }

    private void finish(Long projectId, List<CompletableFuture<Boolean>> futures, long startedAt) {
        long succeeded = futures.stream()
                .filter(future -> !future.isCompletedExceptionally())
                .filter(future -> future.getNow(false))
                .count();

        log.info("[finish] 의견 수집 완료: projectId={}, 성공 {}/{}, 소요 {}ms",
                projectId, succeeded, futures.size(), System.currentTimeMillis() - startedAt);

        OpinionCollectionStatus status = succeeded == futures.size()
                ? OpinionCollectionStatus.COMPLETED
                : OpinionCollectionStatus.FAILED;

        projectRepository.finishOpinionCollection(projectId, status);

        //모두 의견 응답을 가졌을 때 리포트 생성 처리
        if (status == OpinionCollectionStatus.COMPLETED) {
            eventPublisher.publishEvent(new OpinionCollectionCompletedEvent(projectId));
        }
    }

    /**
     * 의견 수집이 완료되는 즉시 저장
     *
     * @param projectId 프로젝트 ID
     * @param target 수집 대상 의견/페르소나
     * @param analysisBrief 자료 분석 브리프
     * @return 저장 성공 여부
     */
    private CompletableFuture<Boolean> collectOne(Long projectId, OpinionCollectionTarget target, String analysisBrief) {
        return CompletableFuture
                .supplyAsync(() -> opinionAiClient.collectOpinion(target.personaId(), analysisBrief),
                        opinionCollectionExecutor)
                .orTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenApply(callResult -> {
                    transactionTemplate.executeWithoutResult(status ->
                            applyResult(target.opinionId(), callResult.result()));
                    saveAiUsageQuietly(projectId, callResult);
                    log.debug("[collectOne] 의견 저장 완료: opinionId={}, personaId={}, model={}, inputTokens={}, outputTokens={}",
                            target.opinionId(), target.personaId(),
                            callResult.model(), callResult.inputTokens(), callResult.outputTokens());
                    return true;
                })
                .exceptionally(throwable -> {
                    log.error("[collectOne] 의견 수집 실패: opinionId={}, personaId={}",
                            target.opinionId(), target.personaId(), throwable);
                    return false;
                });
    }

    private void saveAiUsageQuietly(Long projectId, OpinionCallResult callResult) {
        try {
            aiService.saveAiUsage(new SaveAiUsageCommand(
                    projectId,
                    callResult.model(),
                    AiPurpose.OPINION,
                    callResult.inputTokens(),
                    callResult.outputTokens()));
        } catch (Exception e) {
            log.warn("[saveAiUsageQuietly] AI 사용량을 저장할 수 없음: projectId={}", projectId, e);
        }
    }

    private void applyResult(Long opinionId, OpinionAiResultDto result) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(OpinionNotFoundException::new);

        opinion.updateResult(result.adjustedScore(), result.summary());

        result.toDetails().stream()
                .filter(detail -> StringUtils.hasText(detail.content()))
                .forEach(detail -> opinion.addOpinionDetail(OpinionDetail.builder()
                        .opinion(opinion)
                        .type(detail.type())
                        .content(detail.content())
                        .build()));
    }
}
