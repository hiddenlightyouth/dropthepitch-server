package kr.yuns.dropthepitchserver.opinion.service.ai.dto;

public record OpinionCallResult(
        OpinionAiResultDto result,
        String model,
        int inputTokens,
        int outputTokens
) { }
