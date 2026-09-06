package kr.yuns.dropthepitchserver.persona.service.ai;

import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.persona.data.dto.ai.TagSelectionCallResult;
import kr.yuns.dropthepitchserver.persona.data.repository.PersonaTagRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//실제 Gemini를 호출한다. GEMINI_API_KEY가 있어야 하고 토큰이 소모된다.
//DB에 detail이 채워진 분석이 있으면 그것을, 없으면 샘플 브리프를 쓴다.
@Disabled("실제 Gemini를 호출해 토큰을 쓰고 AI 응답에 단언을 건다. 프롬프트를 고친 뒤 수동으로 실행할 것")
@SpringBootTest
class PersonaMatchingClientTest {

    @Autowired
    private PersonaMatchingClient personaMatchingClient;
    @Autowired
    private PersonaTagRepository personaTagRepository;
    @Autowired
    private AnalysisRepository analysisRepository;

    @Test
    void 분석_브리프로_태그를_고른다() {
        List<String> allTags = personaTagRepository.findDistinctNames();
        String brief = analysisRepository.findAll().stream()
                .filter(a -> a.getDetail() != null)
                .max(Comparator.comparing(Analysis::getId))
                .map(Analysis::getDetail)
                .orElseGet(this::sampleBrief);

        System.out.println("===== 입력 =====");
        System.out.println("브리프 = " + brief.length() + "자, 태그 후보 = " + allTags.size() + "종");

        TagSelectionCallResult callResult = personaMatchingClient.selectTags(allTags, brief);
        List<String> selected = callResult.result().tags();

        System.out.println("===== 결과 =====");
        System.out.println("model = " + callResult.model()
                + ", inputTokens = " + callResult.inputTokens()
                + ", outputTokens = " + callResult.outputTokens());
        System.out.println("고른 태그 " + selected.size() + "개");
        selected.forEach(tag -> System.out.println("  " + tag + (allTags.contains(tag) ? "" : "   << 목록에 없음")));

        List<String> unknown = selected.stream().filter(tag -> !allTags.contains(tag)).toList();
        System.out.println("목록에 없는 태그 = " + unknown.size() + "개 " + unknown);
        System.out.println("중복 = " + (selected.size() - selected.stream().distinct().count()) + "개");

        //스키마로 개수를 강제했고, 태그는 목록 안에서만 고르라고 지시했다.
        assertThat(selected).hasSizeBetween(20, 35);
        assertThat(selected).doesNotHaveDuplicates();
        assertThat(unknown).isEmpty();
    }

    //S3 키가 없어 파일 업로드가 안 되는 동안 쓰는 대체 입력.
    private String sampleBrief() {
        try {
            System.out.println("(DB에 분석 결과가 없어 샘플 브리프를 씁니다)");
            return new ClassPathResource("sample-analysis-detail.json")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("샘플 브리프를 읽지 못했습니다.", e);
        }
    }
}
