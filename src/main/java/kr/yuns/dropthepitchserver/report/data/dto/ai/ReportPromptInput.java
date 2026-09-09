package kr.yuns.dropthepitchserver.report.data.dto.ai;

//리포트 생성 프롬프트에 넣을 값.
public record ReportPromptInput(
        String analysis,
        String opinions,
        int opinionCount
) { }
