package kr.yuns.dropthepitchserver.admin.support;

import java.util.Map;

public final class GeminiPricing {
    private static final double TOKENS_PER_UNIT = 1_000_000.0;

    private static final String DEFAULT_MODEL = "gemini-3.1-flash-lite";

    public record Rate(double input, double output) { }

    private static final Map<String, Rate> RATES = Map.ofEntries(
            Map.entry("gemini-3.8-flash", new Rate(0.75, 3.75)),
            Map.entry("gemini-3.7-flash", new Rate(0.75, 3.75)),
            Map.entry("gemini-3.6-flash", new Rate(0.75, 3.75)),
            Map.entry("gemini-3.5-flash", new Rate(1.50, 9.00)),
            Map.entry("gemini-3-flash-preview", new Rate(0.50, 3.00)),
            Map.entry("gemini-3.5-flash-lite", new Rate(0.30, 2.50)),
            Map.entry("gemini-3.1-flash-lite", new Rate(0.25, 1.50)),
            Map.entry("gemini-3.1-pro-preview", new Rate(2.00, 12.00)),
            Map.entry("gemini-2.5-pro", new Rate(1.25, 10.00)),
            Map.entry("gemini-2.5-flash", new Rate(0.30, 2.50)),
            Map.entry("gemini-2.5-flash-lite", new Rate(0.10, 0.40))
    );

    private static final Map<String, String> DISPLAY_NAMES = Map.of(
            "gemini-3.1-flash-lite", "Gemini 3.1 Flash-Lite",
            "gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite",
            "gemini-2.5-flash-lite", "Gemini 2.5 Flash-Lite",
            "gemini-3.5-flash", "Gemini 3.5 Flash",
            "gemini-2.5-flash", "Gemini 2.5 Flash",
            "gemini-2.5-pro", "Gemini 2.5 Pro"
    );

    private GeminiPricing() {
    }

    public static Rate rateOf(String model) {
        return RATES.getOrDefault(model, RATES.get(DEFAULT_MODEL));
    }

    public static String displayNameOf(String model) {
        return DISPLAY_NAMES.getOrDefault(model, model);
    }

    public static double costOf(String model, long inputTokens, long outputTokens) {
        Rate rate = rateOf(model);
        return inputTokens / TOKENS_PER_UNIT * rate.input() + outputTokens / TOKENS_PER_UNIT * rate.output();
    }
}
