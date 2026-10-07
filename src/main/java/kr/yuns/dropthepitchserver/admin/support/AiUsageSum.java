package kr.yuns.dropthepitchserver.admin.support;

import lombok.Getter;

@Getter
public class AiUsageSum {
    private long calls;
    private long inputTokens;
    private long outputTokens;
    private double totalCost;

    public AiUsageSum add(String model, long calls, long inputTokens, long outputTokens) {
        this.calls += calls;
        this.inputTokens += inputTokens;
        this.outputTokens += outputTokens;
        this.totalCost += GeminiPricing.costOf(model, inputTokens, outputTokens);
        return this;
    }

    public AiUsageSum merge(AiUsageSum other) {
        this.calls += other.calls;
        this.inputTokens += other.inputTokens;
        this.outputTokens += other.outputTokens;
        this.totalCost += other.totalCost;
        return this;
    }

    public long getTotalTokens() {
        return inputTokens + outputTokens;
    }

    public double getRoundedCost() {
        return Math.round(totalCost * 1_000_000) / 1_000_000.0;
    }
}
