package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.InsightResult;
import com.localmind.domain.mcp.port.in.InsightEngineUseCase;
import com.localmind.domain.mcp.port.out.InsightRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service implementing the insight-engine tool group.
 * Provides NL insight queries, metric correlation, trend explanation and health dashboard.
 * Standalone implementation with simulated analysis (no external dependencies).
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class InsightEngineService implements InsightEngineUseCase {

    private final InsightRepository repository;

    public InsightEngineService(InsightRepository repository) {
        this.repository = repository;
    }

    // ========== 1. queryInsight ==========

    @Override
    public Map<String, Object> queryInsight(String question) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (question == null || question.isBlank()) {
            result.put("error", "Question is required");
            return result;
        }

        String lowerQuestion = question.toLowerCase();
        String insight;
        double confidence;
        List<String> suggestedActions = new ArrayList<>();

        if (lowerQuestion.contains("velocity")) {
            insight = "Team velocity analysis shows consistent delivery patterns. " +
                    "The current sprint velocity is aligned with the rolling average.";
            confidence = 0.82;
            suggestedActions.add("Review sprint commitment against capacity");
            suggestedActions.add("Identify velocity outliers in recent sprints");
        } else if (lowerQuestion.contains("budget")) {
            insight = "Budget utilization is within expected parameters. " +
                    "Current burn rate suggests the project will stay within allocated budget.";
            confidence = 0.78;
            suggestedActions.add("Review upcoming planned expenditures");
            suggestedActions.add("Compare actual vs estimated costs per feature");
        } else if (lowerQuestion.contains("sprint")) {
            insight = "Sprint health indicators suggest normal progress. " +
                    "Work items are being completed at a steady rate with acceptable scope changes.";
            confidence = 0.85;
            suggestedActions.add("Monitor remaining story points vs days left");
            suggestedActions.add("Check for blocked items");
        } else if (lowerQuestion.contains("incident")) {
            insight = "Incident analysis indicates a stable system with occasional minor issues. " +
                    "Mean time to resolution has been improving over the last period.";
            confidence = 0.73;
            suggestedActions.add("Review recurring incident patterns");
            suggestedActions.add("Update runbooks for common incident types");
        } else if (lowerQuestion.contains("quality")) {
            insight = "Code quality metrics are within acceptable thresholds. " +
                    "Test coverage and code review practices maintain a good standard.";
            confidence = 0.80;
            suggestedActions.add("Address technical debt in identified areas");
            suggestedActions.add("Review quality gate thresholds");
        } else {
            insight = "Analysis of the question suggests multiple factors at play. " +
                    "Further investigation with specific metrics would provide more precise insights.";
            confidence = 0.60;
            suggestedActions.add("Specify particular metrics of interest");
            suggestedActions.add("Provide a narrower time period for analysis");
        }

        result.put("question", question);
        result.put("insight", insight);
        result.put("confidence", confidence);
        result.put("suggestedActions", suggestedActions);
        result.put("queriedAt", Instant.now().toString());

        // Persist
        InsightResult insightResult = InsightResult.builder()
                .id(UUID.randomUUID().toString())
                .question(question)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(insightResult);

        return result;
    }

    // ========== 2. correlateMetrics ==========

    @Override
    public Map<String, Object> correlateMetrics(String metricsJson, String period) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (metricsJson == null || metricsJson.isBlank()) {
            result.put("error", "Metrics JSON is required");
            return result;
        }

        List<String> metricNames = parseMetricNames(metricsJson);

        if (metricNames.size() < 2) {
            result.put("error", "At least 2 metrics are required for correlation");
            return result;
        }

        List<Map<String, Object>> correlations = new ArrayList<>();
        String[] strengths = {"strong", "moderate", "weak", "negligible"};
        String[] directions = {"positive", "negative"};

        // Generate correlations between all pairs
        int pairIndex = 0;
        for (int i = 0; i < metricNames.size(); i++) {
            for (int j = i + 1; j < metricNames.size(); j++) {
                Map<String, Object> corr = new LinkedHashMap<>();
                corr.put("metricA", metricNames.get(i));
                corr.put("metricB", metricNames.get(j));

                String strength = strengths[pairIndex % strengths.length];
                String direction = directions[pairIndex % directions.length];
                corr.put("strength", strength);
                corr.put("direction", direction);
                corr.put("interpretation", generateCorrelationInterpretation(
                        metricNames.get(i), metricNames.get(j), strength, direction));

                correlations.add(corr);
                pairIndex++;
            }
        }

        result.put("metrics", metricNames);
        result.put("correlations", correlations);
        result.put("period", period != null ? period : "all-time");

        return result;
    }

    // ========== 3. explainTrend ==========

    @Override
    public Map<String, Object> explainTrend(String metric, String direction, String period) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (metric == null || metric.isBlank()) {
            result.put("error", "Metric name is required");
            return result;
        }

        if (direction == null || direction.isBlank()) {
            result.put("error", "Direction is required");
            return result;
        }

        String explanation;
        List<String> contributingFactors = new ArrayList<>();
        double confidence;

        String lowerDirection = direction.toLowerCase();

        if ("increasing".equals(lowerDirection)) {
            explanation = "The " + metric + " metric shows an increasing trend during " +
                    (period != null ? period : "the observed period") +
                    ". This upward movement is consistent with recent changes in the project.";
            contributingFactors.add("Recent code changes and feature additions");
            contributingFactors.add("Team size or effort adjustments");
            contributingFactors.add("Process improvements taking effect");
            confidence = 0.75;
        } else if ("decreasing".equals(lowerDirection)) {
            explanation = "The " + metric + " metric shows a decreasing trend during " +
                    (period != null ? period : "the observed period") +
                    ". This downward movement may require attention depending on whether lower values are desirable.";
            contributingFactors.add("Reduced input or resource allocation");
            contributingFactors.add("Complexity increases in the codebase");
            contributingFactors.add("Seasonal or cyclical patterns");
            confidence = 0.70;
        } else {
            explanation = "The " + metric + " metric shows a stable trend during " +
                    (period != null ? period : "the observed period") +
                    ". This stability suggests consistent practices and predictable outcomes.";
            contributingFactors.add("Established processes and routines");
            contributingFactors.add("Consistent team composition");
            contributingFactors.add("Balanced workload distribution");
            confidence = 0.85;
        }

        result.put("metric", metric);
        result.put("direction", direction);
        result.put("explanation", explanation);
        result.put("contributingFactors", contributingFactors);
        result.put("confidence", confidence);
        result.put("period", period != null ? period : "all-time");

        return result;
    }

    // ========== 4. healthDashboard ==========

    @Override
    public Map<String, Object> healthDashboard() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> areas = new ArrayList<>();

        areas.add(buildArea("Sprint Progress", "good", 85,
                "Current sprint is on track with expected velocity"));
        areas.add(buildArea("Code Quality", "good", 90,
                "Quality metrics are above threshold levels"));
        areas.add(buildArea("CI/CD Pipeline", "good", 92,
                "Build success rate is high with acceptable pipeline duration"));
        areas.add(buildArea("Budget", "warning", 65,
                "Budget utilization approaching planned limits"));
        areas.add(buildArea("Incident Response", "good", 88,
                "Mean time to resolution is within SLA targets"));
        areas.add(buildArea("Team Velocity", "good", 80,
                "Velocity is consistent with rolling average"));

        // Calculate overall health
        int totalScore = 0;
        boolean hasWarning = false;
        boolean hasCritical = false;

        for (Map<String, Object> area : areas) {
            totalScore += (int) area.get("score");
            String status = (String) area.get("status");
            if ("warning".equals(status)) hasWarning = true;
            if ("critical".equals(status)) hasCritical = true;
        }

        int avgScore = totalScore / areas.size();
        String overallHealth;
        if (hasCritical || avgScore < 50) {
            overallHealth = "critical";
        } else if (hasWarning || avgScore < 75) {
            overallHealth = "warning";
        } else {
            overallHealth = "good";
        }

        result.put("overallHealth", overallHealth);
        result.put("overallScore", avgScore);
        result.put("areas", areas);
        result.put("generatedAt", Instant.now().toString());

        return result;
    }

    // ========== Helpers ==========

    private Map<String, Object> buildArea(String name, String status, int score, String details) {
        Map<String, Object> area = new LinkedHashMap<>();
        area.put("name", name);
        area.put("status", status);
        area.put("score", score);
        area.put("details", details);
        return area;
    }

    /**
     * Parses a JSON array of metric name strings.
     * Expected format: ["velocity","coverage","bugs"]
     */
    List<String> parseMetricNames(String json) {
        List<String> names = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return names;
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return names;
        }

        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) {
            return names;
        }

        String[] parts = inner.split(",");
        for (String part : parts) {
            String name = part.trim().replace("\"", "");
            if (!name.isEmpty()) {
                names.add(name);
            }
        }

        return names;
    }

    private String generateCorrelationInterpretation(String metricA, String metricB,
                                                      String strength, String direction) {
        return metricA + " and " + metricB + " show a " + strength + " " + direction +
                " correlation, suggesting that changes in one may " +
                ("strong".equals(strength) ? "significantly" : "partially") +
                " affect the other.";
    }

    // ========== JSON Serialization ==========

    @SuppressWarnings("unchecked")
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
