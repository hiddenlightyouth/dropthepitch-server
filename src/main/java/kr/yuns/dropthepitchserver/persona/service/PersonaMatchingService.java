package kr.yuns.dropthepitchserver.persona.service;

import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.persona.data.dto.ai.TagSelectionCallResult;
import kr.yuns.dropthepitchserver.persona.data.repository.PersonaTagRepository;
import kr.yuns.dropthepitchserver.persona.service.ai.PersonaMatchingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaMatchingService {

    private final PersonaTagRepository personaTagRepository;
    private final OpinionRepository opinionRepository;
    private final PersonaMatchingClient personaMatchingClient;
    private final PersonaMatchingResultService resultService;

    /* 분석 결과에 맞는 페르소나를 연령대별로 뽑아 의견 자리를 생성*/
    public void match(Long projectId) {
        long start = System.currentTimeMillis();

        if (opinionRepository.existsByProject_Id(projectId)) {
            log.warn("[match] 이미 선별이 끝난 프로젝트입니다: projectId={}", projectId);
            return;
        }

        String brief = resultService.getAnalysisBrief(projectId);
        List<String> allTags = personaTagRepository.findDistinctNames();

        TagSelectionCallResult callResult = personaMatchingClient.selectTags(allTags, brief);
        List<String> tags = keepKnownTags(callResult.result().tags(), allTags);

        resultService.saveSelection(projectId, tags, callResult);

        log.info("[match] 페르소나 선별 완료: projectId={}, {}ms, model={}, inputTokens={}, outputTokens={}",
                projectId, System.currentTimeMillis() - start,
                callResult.model(), callResult.inputTokens(), callResult.outputTokens());
    }

    //태그 걸러내는 로직
    private List<String> keepKnownTags(List<String> selected, List<String> allTags) {
        Set<String> known = Set.copyOf(allTags);
        List<String> kept = selected.stream().filter(known::contains).distinct().toList();

        if (kept.size() < selected.size()) {
            log.warn("[match] 쓸 수 없는 태그를 걸렀습니다: 받은 {}개 중 {}개 사용", selected.size(), kept.size());
        }
        //태그 조건 없이 연령대별로 무작위 선별된다. 화면이 비는 것보다는 낫다.
        if (kept.isEmpty()) {
            log.error("[match] 쓸 수 있는 태그가 하나도 없어 무작위로 선별합니다.");
        }
        return kept;
    }
}
