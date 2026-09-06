package kr.yuns.dropthepitchserver.analyze.event;
// 분석 끝난 유무 전달, -> 다음 flow 페르소나 선별
public record AnalysisCompletedEvent(Long projectId) { }
