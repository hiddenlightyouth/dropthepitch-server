package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.ai.service.AiService;
import kr.yuns.dropthepitchserver.ai.service.dto.SaveAiUsageCommand;
import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.analyze.data.exception.FileAnalysisFailedException;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.analyze.service.ai.AnalysisPromptLoader;
import kr.yuns.dropthepitchserver.analyze.service.ai.GeminiAnalysisClient;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileAnalysisService {

    private final AnalysisPromptLoader promptLoader;
    private final GeminiAnalysisClient geminiAnalysisClient;
    private final AnalysisResultService analysisResultService;
    private final VideoDurationReader videoDurationReader;
    private final AiService aiService;

    private static final int MIN_TEXT_LENGTH = 20;

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

        if (isEmptyText(type, bytes)) {
            analysisResultService.saveRejected(projectId, AnalysisVerdict.NO_CONTENT);
            log.info("[analyze] 내용이 없어 AI를 부르지 않습니다: projectId={}", projectId);
            return;
        }

        if (isUnreadablePdf(type, bytes)) {
            analysisResultService.saveRejected(projectId, AnalysisVerdict.UNREADABLE);
            log.info("[analyze] 열 수 없는 PDF라 AI를 부르지 않습니다: projectId={}", projectId);
            return;
        }

        try {
            Media media = new Media(promptLoader.mimeType(type), new ByteArrayResource(bytes));

            AnalysisCallResult callResult = geminiAnalysisClient.analyze(
                    promptLoader.systemPrompt(),
                    promptLoader.userPrompt(type),
                    promptLoader.schema(),
                    List.of(media));

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
            saveFailureQuietly(projectId, e instanceof FileAnalysisFailedException failed ? failed.getReason() : AnalysisVerdict.AI_ERROR);
        }
    }

    private void saveFailureQuietly(Long projectId, AnalysisVerdict reason) {
        try {
            analysisResultService.saveFailure(projectId, reason);
        } catch (Exception e) {
            log.error("[analyze] 실패 상태 기록마저 실패: projectId={}", projectId, e);
        }
    }

    private boolean isUnreadablePdf(InputType type, byte[] bytes) {
        if (type != InputType.PDF) {
            return false;
        }
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return document.getNumberOfPages() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    private boolean isEmptyText(InputType type, byte[] bytes) {
        return type == InputType.MD && new String(bytes, StandardCharsets.UTF_8).strip().length() < MIN_TEXT_LENGTH;
    }
}
