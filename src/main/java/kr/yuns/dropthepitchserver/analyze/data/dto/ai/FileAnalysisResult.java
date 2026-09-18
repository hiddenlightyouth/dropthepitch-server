package kr.yuns.dropthepitchserver.analyze.data.dto.ai;

import java.util.List;

//필드 이름은 스키마의 키와 반드시 같아야 한다.
public record FileAnalysisResult(
        String title,
        String summary,
        List<String> keywords,
        List<TimelineSegment> timeline,
        AnalysisDetail detail,
        Review review
) {
    public record TimelineSegment(
            String startTime,
            String endTime,
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

    public record Review(
            String verdict,
            String reason
    ) { }

    public record Assumption(
            String assumption,
            String basis
    ) { }
}
