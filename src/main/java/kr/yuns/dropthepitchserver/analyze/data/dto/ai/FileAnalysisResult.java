package kr.yuns.dropthepitchserver.analyze.data.dto.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

//AI가 반환할 분석 결과. 이 구조가 그대로 JSON 스키마로 변환되어 요청에 실린다.
public record   FileAnalysisResult(
        @JsonPropertyDescription("사이드바 목록에서 이 작업을 알아볼 수 있는 이름. 브랜드나 제품명이 있으면 포함. 한국어 20자 이내")
        String title,

        @JsonPropertyDescription("화면에 그대로 표시되는 한 줄 요약. '~해요'로 끝나는 해요체 한 문장, 최대 두 문장, 80자 안팎")
        String summary,

        @JsonPropertyDescription("다음 단계에서 페르소나들이 기획안을 평가할 때 읽는 상세 브리프. 자료에 없는 항목은 '자료에 명시되지 않음'")
        AnalysisDetail detail,

        @JsonPropertyDescription("업종, 타깃, 핵심 특징을 나타내는 한국어 명사 3~5개")
        List<String> keywords,

        @JsonPropertyDescription("영상일 때만 시간순 구간 설명. 영상이 아니면 빈 배열")
        List<TimelineSegment> timeline
) {
    public record AnalysisDetail(
            @JsonPropertyDescription("제안하는 제품, 서비스, 캠페인이 무엇인지. 브랜드명과 제품명은 원문 표기 그대로")
            String offering,

            @JsonPropertyDescription("주요 타깃 고객. 연령, 성별, 상황, 직업 등 자료에 드러난 대로")
            String targetAudience,

            @JsonPropertyDescription("해결하려는 문제 또는 노리는 기회")
            String problemOrOpportunity,

            @JsonPropertyDescription("핵심 기능, 차별점, 소구 포인트. 자료의 숫자와 문구를 그대로 인용")
            String keyPoints,

            @JsonPropertyDescription("가격, 유통, 출시 시점, 프로모션 등 사업 정보")
            String businessInfo,

            @JsonPropertyDescription("톤앤매너, 등장인물, 시각 및 청각 연출 등 표현 방식. 문서는 구성과 디자인")
            String presentation,

            @JsonPropertyDescription("판독이 불확실했거나 자료에서 확인하지 못한 점. 없으면 빈 문자열")
            String uncertainties
    ) { }

    public record TimelineSegment(
            @JsonPropertyDescription("구간 시작 시각. 초 단위 정수")
            Integer startTime,

            @JsonPropertyDescription("구간 끝 시각. 초 단위 정수이며 시작보다 커야 한다")
            Integer endTime,

            @JsonPropertyDescription("이 구간의 화면과 음성 내용, 그리고 기획상 의미. 한국어 150자 이내")
            String content
    ) { }
}
