package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.analyze.service.ai.AnalysisPromptLoader;
import kr.yuns.dropthepitchserver.analyze.service.ai.GeminiAnalysisClient;
import kr.yuns.dropthepitchserver.common.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.List;

//S3에서 파일을 받아 AI에 보내고 결과를 저장하기까지의 흐름을 지휘한다.
//@Transactional을 붙이지 않는다. AI 호출이 수십 초 걸리는데 그동안 DB 커넥션을 잡고 있으면
//커넥션 풀이 마른다. DB 작업은 AnalysisResultService의 짧은 트랜잭션에 맡긴다.
@Service
@Slf4j
@RequiredArgsConstructor
public class FileAnalysisService {

    //Gemini는 전체 요청 크기가 100MB(PDF는 50MB)를 넘으면 인라인 전송을 받지 않는다.
    //프롬프트와 인코딩 여유를 두고 한도보다 낮게 잡는다. 넘는 파일은 Files API가 필요하다.
    private static final int INLINE_MAX_BYTES = 80 * 1024 * 1024;
    private static final int INLINE_MAX_BYTES_PDF = 40 * 1024 * 1024;

    private final S3Service s3Service;
    private final AnalysisPromptLoader promptLoader;
    private final GeminiAnalysisClient geminiAnalysisClient;
    private final AnalysisResultService analysisResultService;

    /**
     * 업로드된 파일을 분석해 결과를 저장합니다.
     * 어떤 이유로 실패하더라도 예외를 밖으로 던지지 않고 상태를 FAILED로 남깁니다.
     *
     * @param projectId 프로젝트 ID
     */
    public void analyze(Long projectId) {
        long start = System.currentTimeMillis();
        log.info("[analyze] 분석 시작: projectId={}, thread={}", projectId, Thread.currentThread());

        try {
            File file = analysisResultService.getFile(projectId);
            InputType type = file.getType();

            //내려받기 전에 크기부터 본다. 업로드는 1GB까지 허용하므로 먼저 걸러야 메모리가 터지지 않는다.
            int limit = type == InputType.PDF ? INLINE_MAX_BYTES_PDF : INLINE_MAX_BYTES;
            if (file.getSize() > limit) {
                log.error("[analyze] 인라인 전송 한도 초과: projectId={}, type={}, size={}bytes, limit={}bytes",
                        projectId, type, file.getSize(), limit);
                analysisResultService.saveFailure(projectId);
                return;
            }

            byte[] bytes = s3Service.download(file.getUrl());

            Media media = new Media(promptLoader.mimeType(type), new ByteArrayResource(bytes));

            AnalysisCallResult callResult = geminiAnalysisClient.analyze(
                    promptLoader.systemPrompt(),
                    promptLoader.userPrompt(type),
                    promptLoader.schema(),
                    List.of(media));

            analysisResultService.saveSuccess(projectId, callResult);

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
