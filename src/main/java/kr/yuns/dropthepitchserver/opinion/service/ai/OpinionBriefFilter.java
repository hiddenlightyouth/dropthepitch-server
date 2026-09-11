package kr.yuns.dropthepitchserver.opinion.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpinionBriefFilter {
    private static final List<String> REMOVED_FIELDS = List.of("informationGaps", "discussionPoints");
    private static final List<String> REMOVED_CLAIM_FIELDS = List.of("evidenceType", "evidence");

    private final ObjectMapper objectMapper;

    /**
     * 페르소나에게 넘길 브리프에서 이미 정리된 평가 항목을 걷어냅니다.
     * 서른 명이 같은 목록을 그대로 복창하지 않도록, 자료에 무엇이 있는지만 남깁니다.
     *
     * @param brief 분석 브리프 JSON
     * @return 평가 항목을 제거한 브리프
     */
    public String filter(String brief) {
        try {
            JsonNode root = objectMapper.readTree(brief);

            if (!(root.path("detail") instanceof ObjectNode detail)) {
                log.warn("[filter] detail이 없는 브리프입니다. 원본을 그대로 사용합니다.");
                return brief;
            }

            REMOVED_FIELDS.forEach(detail::remove);
            detail.path("claims").forEach(claim -> {
                if (claim instanceof ObjectNode claimNode) {
                    claimNode.remove(REMOVED_CLAIM_FIELDS);
                }
            });

            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            log.warn("[filter] 브리프를 걸러낼 수 없어 원본을 사용합니다: {}", e.getMessage());
            return brief;
        }
    }
}
