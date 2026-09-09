package kr.yuns.dropthepitchserver.report.service;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.ai.service.AiService;
import kr.yuns.dropthepitchserver.ai.service.dto.SaveAiUsageCommand;
import kr.yuns.dropthepitchserver.report.data.dto.ai.ReportGenerationCallResult;
import kr.yuns.dropthepitchserver.report.data.dto.ai.ReportPromptInput;
import kr.yuns.dropthepitchserver.report.service.ai.ReportGenerationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//리포트 생성
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationService {

    private final ReportGenerationResultService resultService;
    private final ReportGenerationClient reportGenerationClient;
    private final AiService aiService;

    /**
     * 수집된 의견을 모아 리포트를 만듭니다.
     *
     * @param projectId 프로젝트 ID
     */
    public void generate(Long projectId) {
        long start = System.currentTimeMillis();

        if (resultService.isAlreadyGenerated(projectId)) {
            log.warn("[generate] 이미 만들어진 리포트입니다: projectId={}", projectId);
            return;
        }

        ReportPromptInput input = resultService.getPromptInput(projectId);

        if (input.opinionCount() == 0) {
            log.warn("[generate] 수집된 의견이 없어 리포트를 만들지 않습니다: projectId={}", projectId);
            return;
        }

        ReportGenerationCallResult callResult =
                reportGenerationClient.generate(input.analysis(), input.opinions());

        resultService.saveReport(projectId, callResult.result());
        saveAiUsage(projectId, callResult);

        log.info("[generate] 리포트 생성 완료: projectId={}, {}ms, model={}, inputTokens={}, outputTokens={}",
                projectId, System.currentTimeMillis() - start,
                callResult.model(), callResult.inputTokens(), callResult.outputTokens());
    }

    private void saveAiUsage(Long projectId, ReportGenerationCallResult callResult) {
        try {
            aiService.saveAiUsage(new SaveAiUsageCommand(
                    projectId,
                    callResult.model(),
                    AiPurpose.REPORT,
                    callResult.inputTokens(),
                    callResult.outputTokens()));
        } catch (Exception e) {
            log.warn("[saveAiUsage] 사용량 저장 실패, 리포트 생성은 계속합니다: projectId={}", projectId, e);
        }
    }
}
