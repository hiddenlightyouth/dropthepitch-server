package kr.yuns.dropthepitchserver.analyze.data.dto.ai;

import java.util.List;

//AI가 돌려준 분석 결과를 담는 그릇.
//필드 설명과 작성 규칙은 resources/schema/analysis-schema.json에 있다.
//여기에 설명을 적지 않는 이유: 스키마를 자바에서 생성하지 않고 JSON 파일을 그대로 보내기 때문이다.
//필드 이름은 스키마의 키와 반드시 같아야 한다.
public record FileAnalysisResult(
        String title,
        String summary,
        List<String> keywords,
        List<TimelineSegment> timeline,
        AnalysisDetail detail
) {
    //영상 구간. analysis_timeline 테이블에 그대로 저장된다.
    public record TimelineSegment(
            Integer startTime,
            Integer endTime,
            String content
    ) { }

    public record AnalysisDetail(
            String offering,
            String category,
            String materialAudience,
            String endConsumer,
            String consumerProblem,
            String providerContext,
            List<Claim> claims,
            String businessInfo,
            String presentation,
            List<GlossaryEntry> glossary,
            String targetAudience,
            UserAssumptions userAssumptions,
            List<String> discussionPoints,
            List<String> informationGaps,
            List<String> uncertainties
    ) { }

    //자료가 세상에 대해 말하는 검증 가능한 주장과 그 근거.
    public record Claim(
            String claim,
            String evidenceType,
            String evidence,
            String source
    ) { }

    public record GlossaryEntry(
            String term,
            String explanationInMaterial,
            String context
    ) { }

    //자료가 사용자에게 요구하는 조건을 여덟 차원으로 나눈 것.
    //같은 Assumption을 여덟 번 쓰기 때문에 스키마를 자바에서 생성하면 $ref가 나온다.
    public record UserAssumptions(
            List<Assumption> cost,
            List<Assumption> time,
            List<Assumption> health,
            List<Assumption> digital,
            List<Assumption> region,
            List<Assumption> trust,
            List<Assumption> eligibility,
            List<Assumption> knowledge
    ) { }

    public record Assumption(
            String assumption,
            String basis
    ) { }
}
