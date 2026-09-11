package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.ai.service.AiService;
import kr.yuns.dropthepitchserver.ai.service.dto.SaveAiUsageCommand;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.analyze.service.ai.AnalysisPromptLoader;
import kr.yuns.dropthepitchserver.analyze.service.ai.GeminiAnalysisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.List;

//파일 내용을 AI에 보내고 결과를 저장하기까지의 흐름을 지휘한다.
//파일 내용은 UploadedFileProcessor가 한 번 내려받아 넘겨준다.
//@Transactional을 붙이지 않는다. AI 호출이 수십 초 걸리는데 그동안 DB 커넥션을 잡고 있으면
//커넥션 풀이 마른다. DB 작업은 AnalysisResultService의 짧은 트랜잭션에 맡긴다.
@Service
@Slf4j
@RequiredArgsConstructor
public class FileAnalysisService {

    private final AnalysisPromptLoader promptLoader;
    private final GeminiAnalysisClient geminiAnalysisClient;
    private final AnalysisResultService analysisResultService;
    private final VideoDurationReader videoDurationReader;
    private final AiService aiService;

    /**
     * 파일 내용을 분석해 결과를 저장합니다.
     * 어떤 이유로 실패하더라도 예외를 밖으로 던지지 않고 상태를 FAILED로 남깁니다.
     *
     * @param projectId 프로젝트 ID
     * @param file 업로드된 파일
     * @param bytes 파일 내용
     */
    public void analyze(Long projectId, File file, byte[] bytes) {
        long start = System.currentTimeMillis();
        InputType type = file.getType();
        Integer videoSeconds = type == InputType.MP4 ? videoDurationReader.readSeconds(bytes) : null;
        log.info("[analyze] 분석 시작: projectId={}, videoSeconds={}, thread={}", projectId, videoSeconds, Thread.currentThread());

        try {
            Media media = new Media(promptLoader.mimeType(type), new ByteArrayResource(bytes));

            AnalysisCallResult callResult = geminiAnalysisClient.analyze(
                    promptLoader.systemPrompt(),
                    promptLoader.userPrompt(type, videoSeconds),
                    promptLoader.schema(),
                    List.of(media));

            //결과 저장이 실패해도 토큰을 쓴 기록은 남도록 먼저 따로 남긴다.
            aiService.saveAiUsage(new SaveAiUsageCommand(
                    projectId,
                    callResult.model(),
                    AiPurpose.ANALYSIS,
                    callResult.inputTokens(),
                    callResult.outputTokens()));
            analysisResultService.saveSuccess(projectId, callResult, videoSeconds);

            log.info("[analyze] 분석 완료: projectId={}, {}ms, model={}, inputTokens={}, outputTokens={}",
                    projectId, System.currentTimeMillis() - start,
                    callResult.model(), callResult.inputTokens(), callResult.outputTokens());

        } catch (Exception e) {
            log.error("[analyze] 분석 실패: projectId={}", projectId, e);
            saveFailureQuietly(projectId);
        }
    }

    //실패를 기록하다가 또 실패하면 원래 오류가 묻힌다. 여기서 끊는다.
    private void saveFailureQuietly(Long projectId) {
        try {
            analysisResultService.saveFailure(projectId);
        } catch (Exception e) {
            log.error("[analyze] 실패 상태 기록마저 실패: projectId={}", projectId, e);
        }
    }
}
