package kr.yuns.dropthepitchserver.analyze;

import kr.yuns.dropthepitchserver.analyze.data.dto.ai.AnalysisCallResult;
import kr.yuns.dropthepitchserver.analyze.service.ai.GeminiAnalysisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//[임시] GeminiAnalysisClient 동작 확인용. 실제 트리거가 생기면 삭제한다.
@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiSpikeRunner implements CommandLineRunner {

    private final GeminiAnalysisClient geminiAnalysisClient;

    private static final String SYSTEM_PROMPT = """
            당신은 기획안 분석가입니다.
            자료에 실제로 있는 내용만 정리하고, 없는 내용은 추측하지 않습니다.
            summary는 '~해요'로 끝나는 해요체 한 문장으로 씁니다.
            영상이 아니므로 timeline은 빈 배열로 둡니다.
            """;

    private static final String SAMPLE_DOCUMENT = """
            한끼박스 사업기획안

            1. 제품: 20대 1인 가구를 위한 주간 반찬 구독 서비스 '한끼박스'
            2. 문제: 자취생은 반찬을 직접 만들기 번거롭고, 마트에서 사면 양이 많아 버리게 된다.
            3. 차별점: 1인분씩 소분 포장, 매주 월요일 새벽 배송, 국내산 재료만 사용
            4. 가격: 월 39,000원 (주 3회 배송, 회당 반찬 3종)
            5. 출시 계획: 2026년 10월 베타, 2027년 1월 정식 출시
            """;

    @Override
    public void run(String... args) {
        log.info("[GeminiSpike] 분석 클라이언트 호출 시작");
        long start = System.currentTimeMillis();

        AnalysisCallResult callResult = geminiAnalysisClient.analyze(
                SYSTEM_PROMPT,
                "아래 기획안을 분석하세요.\n\n" + SAMPLE_DOCUMENT,
                List.of());

        log.info("[GeminiSpike] 소요 시간: {}ms", System.currentTimeMillis() - start);
        log.info("[GeminiSpike] summary  : {}", callResult.result().summary());
        log.info("[GeminiSpike] keywords : {}", callResult.result().keywords());
        log.info("[GeminiSpike] model={}, inputTokens={}, outputTokens={}",
                callResult.model(), callResult.inputTokens(), callResult.outputTokens());
    }
}
