package kr.yuns.dropthepitchserver.opinion.service;

import kr.yuns.dropthepitchserver.opinion.config.OpinionAsyncConfiguration;
import kr.yuns.dropthepitchserver.opinion.data.dto.projection.OpinionCollectionTarget;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import kr.yuns.dropthepitchserver.opinion.data.entity.OpinionDetail;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.opinion.data.exception.OpinionNotFoundException;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.opinion.service.ai.OpinionAiClient;
import kr.yuns.dropthepitchserver.opinion.service.ai.dto.OpinionAiResultDto;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Qualifier(OpinionAsyncConfiguration.OPINION_COLLECTION_EXECUTOR)
    private final Executor opinionCollectionExecutor;

    //HTTP 타임아웃 120s * 재시도 3회 + 백오프(2s, 10s) 보다 넉넉하게 잡습니다.
    private static final long CALL_TIMEOUT_SECONDS = 420L;

    /**
     * 페르소나별 의견 수집을 백그라운드에서 실행합니다.
     * 호출 즉시 반환하고, 각 페르소나의 응답은 도착하는 즉시 개별 트랜잭션으로 저장됩니다.
     *
     * @param projectId 프로젝트 ID
     * @param targets 수집 대상 의견/페르소나 목록
     * @param ideaDescription 아이디어 설명
     */
    public void run(Long projectId, List<OpinionCollectionTarget> targets, String ideaDescription) {
        long startedAt = System.currentTimeMillis();
        log.info("[run] 의견 수집 시작: projectId={}, 대상 {}건", projectId, targets.size());

        List<CompletableFuture<Boolean>> futures = targets.stream()
                .map(target -> collectOne(projectId, target, ideaDescription))
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

        projectRepository.finishOpinionCollection(projectId,
                succeeded == futures.size()
                        ? OpinionCollectionStatus.COMPLETED
                        : OpinionCollectionStatus.FAILED);
    }

    /**
     * 페르소나 한 명의 의견을 수집하고 곧바로 저장합니다.
     *
     * @param projectId 프로젝트 ID
     * @param target 수집 대상 의견/페르소나
     * @param ideaDescription 아이디어 설명
     * @return 저장 성공 여부
     */
    private CompletableFuture<Boolean> collectOne(Long projectId, OpinionCollectionTarget target, String ideaDescription) {
        return CompletableFuture
                .supplyAsync(() -> opinionAiClient.callAiModel(projectId, target.personaId(), ideaDescription),
                        opinionCollectionExecutor)
                .orTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenApply(result -> {
                    transactionTemplate.executeWithoutResult(status -> applyResult(target.opinionId(), result));
                    log.debug("[collectOne] 의견 저장 완료: opinionId={}, personaId={}",
                            target.opinionId(), target.personaId());
                    return true;
                })
                .exceptionally(throwable -> {
                    log.error("[collectOne] 의견 수집 실패: opinionId={}, personaId={}",
                            target.opinionId(), target.personaId(), throwable);
                    return false;
                });
    }

    private void applyResult(Long opinionId, OpinionAiResultDto result) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(OpinionNotFoundException::new);

        opinion.updateResult(toSentiment(result.score()), result.summary());

        if (result.details() == null) {
            return;
        }

        result.details().stream()
                .filter(detail -> detail.type() != null && StringUtils.hasText(detail.content()))
                .forEach(detail -> opinion.addOpinionDetail(OpinionDetail.builder()
                        .opinion(opinion)
                        .type(detail.type())
                        .content(detail.content())
                        .build()));
    }

    private Sentiment toSentiment(Double score) {
        if (score == null) {
            return Sentiment.NEUTRAL;
        }
        return Sentiment.from(score);
    }
}
