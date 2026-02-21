package com.localmind.domain.common.model;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AnalyticsOverview {
    private long totalConversations;
    private long totalDocuments;
    private long totalTokens;
    private double totalCost;
    private double avgResponseTimeMs;
    private Map<String, Long> tokensByProvider;
    private Map<String, Double> costByProvider;
    private Map<String, Long> documentsByStatus;
    private List<UsageDataPoint> usageTimeline;
    private List<ModelUsageEntry> topModels;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UsageDataPoint {
        private String date;
        private long tokens;
        private double cost;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ModelUsageEntry {
        private String model;
        private String provider;
        private long totalTokens;
        private long requestCount;
    }
}
