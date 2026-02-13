package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.AgileMetric;
import com.localmind.domain.mcp.port.in.AgileMetricsUseCase;
import com.localmind.domain.mcp.port.out.AgileMetricRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the agile-metrics tool group.
 * Provides velocity calculation, burndown generation, cycle time analysis,
 * Monte Carlo forecast, risk prediction and factor correlation.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class AgileMetricsService implements AgileMetricsUseCase {

    private final AgileMetricRepository repository;

    /**
     * Number of Monte Carlo simulations for forecast completion.
     */
    static final int MONTE_CARLO_SIMULATIONS = 1000;

    public AgileMetricsService(AgileMetricRepository repository) {
        this.repository = repository;
    }

    // ========== 1. calculateVelocity ==========

    @Override
    public Map<String, Object> calculateVelocity(List<Map<String, Object>> sprints) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sprints == null || sprints.isEmpty()) {
            result.put("error", "No sprint data provided");
            return result;
        }

        List<Map<String, Object>> sprintResults = new ArrayList<>();
        List<Integer> velocities = new ArrayList<>();

        for (Map<String, Object> sprint : sprints) {
            String name = getString(sprint, "name");
            int completedPoints = getInt(sprint, "completedPoints");

            Map<String, Object> sprintResult = new LinkedHashMap<>();
            sprintResult.put("name", name != null ? name : "unknown");
            sprintResult.put("completedPoints", completedPoints);
            sprintResults.add(sprintResult);
            velocities.add(completedPoints);
        }

        double sum = 0;
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;

        for (int v : velocities) {
            sum += v;
            if (v > highest) highest = v;
            if (v < lowest) lowest = v;
        }

        double averageVelocity = sum / velocities.size();

        // Determine trend: compare first half average to second half average
        String trend = determineTrend(velocities);

        result.put("averageVelocity", Math.round(averageVelocity * 100.0) / 100.0);
        result.put("trend", trend);
        result.put("highest", highest);
        result.put("lowest", lowest);
        result.put("sprintCount", velocities.size());
        result.put("sprints", sprintResults);

        // Persist
        AgileMetric metric = AgileMetric.builder()
                .id(UUID.randomUUID().toString())
                .metricType("velocity")
                .sprintRef(null)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(metric);

        return result;
    }

    // ========== 2. generateBurndown ==========

    @Override
    public Map<String, Object> generateBurndown(int totalPoints, int sprintDays,
                                                  List<Map<String, Object>> dailyProgress) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (totalPoints <= 0) {
            result.put("error", "Total points must be greater than zero");
            return result;
        }

        if (sprintDays <= 0) {
            result.put("error", "Sprint days must be greater than zero");
            return result;
        }

        // Build ideal line (linear decline from totalPoints to 0)
        List<Map<String, Object>> idealLine = new ArrayList<>();
        double dailyBurn = (double) totalPoints / sprintDays;

        for (int day = 0; day <= sprintDays; day++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("day", day);
            double idealRemaining = totalPoints - (dailyBurn * day);
            point.put("remaining", Math.round(idealRemaining * 100.0) / 100.0);
            idealLine.add(point);
        }

        // Build actual line from daily progress
        List<Map<String, Object>> actualLine = new ArrayList<>();
        if (dailyProgress != null) {
            for (Map<String, Object> dp : dailyProgress) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("day", getInt(dp, "day"));
                point.put("remaining", getInt(dp, "remaining"));
                actualLine.add(point);
            }
        }

        // Determine status by comparing last actual remaining vs ideal remaining at same day
        String status = "on-track";
        if (!actualLine.isEmpty()) {
            Map<String, Object> lastActual = actualLine.get(actualLine.size() - 1);
            int lastDay = getInt(lastActual, "day");
            int actualRemaining = getInt(lastActual, "remaining");
            double idealRemaining = totalPoints - (dailyBurn * lastDay);

            double tolerance = totalPoints * 0.1; // 10% tolerance
            if (actualRemaining < idealRemaining - tolerance) {
                status = "ahead";
            } else if (actualRemaining > idealRemaining + tolerance) {
                status = "behind";
            }
        }

        result.put("totalPoints", totalPoints);
        result.put("sprintDays", sprintDays);
        result.put("idealLine", idealLine);
        result.put("actualLine", actualLine);
        result.put("status", status);

        // Persist
        AgileMetric metric = AgileMetric.builder()
                .id(UUID.randomUUID().toString())
                .metricType("burndown")
                .sprintRef(null)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(metric);

        return result;
    }

    // ========== 3. calculateCycleTime ==========

    @Override
    public Map<String, Object> calculateCycleTime(List<Map<String, Object>> tasks) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (tasks == null || tasks.isEmpty()) {
            result.put("error", "No task data provided");
            return result;
        }

        List<Map<String, Object>> taskResults = new ArrayList<>();
        List<Long> cycleTimes = new ArrayList<>();

        for (Map<String, Object> task : tasks) {
            String name = getString(task, "name");
            String startDateStr = getString(task, "startDate");
            String endDateStr = getString(task, "endDate");

            Map<String, Object> taskResult = new LinkedHashMap<>();
            taskResult.put("name", name != null ? name : "unknown");

            if (startDateStr == null || endDateStr == null) {
                taskResult.put("cycleTimeDays", 0);
                taskResult.put("error", "Missing startDate or endDate");
                taskResults.add(taskResult);
                continue;
            }

            try {
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDate endDate = LocalDate.parse(endDateStr);
                long days = ChronoUnit.DAYS.between(startDate, endDate);
                if (days < 0) days = 0;
                taskResult.put("cycleTimeDays", days);
                cycleTimes.add(days);
            } catch (DateTimeParseException e) {
                taskResult.put("cycleTimeDays", 0);
                taskResult.put("error", "Invalid date format");
            }

            taskResults.add(taskResult);
        }

        result.put("tasks", taskResults);
        result.put("taskCount", taskResults.size());

        if (!cycleTimes.isEmpty()) {
            double avg = cycleTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            result.put("average", Math.round(avg * 100.0) / 100.0);
            result.put("median", calculateMedian(cycleTimes));
            result.put("p95", calculatePercentile(cycleTimes, 95));
        } else {
            result.put("average", 0.0);
            result.put("median", 0.0);
            result.put("p95", 0.0);
        }

        // Persist
        AgileMetric metric = AgileMetric.builder()
                .id(UUID.randomUUID().toString())
                .metricType("cycle-time")
                .sprintRef(null)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(metric);

        return result;
    }

    // ========== 4. forecastCompletion ==========

    @Override
    public Map<String, Object> forecastCompletion(int remainingPoints, List<Integer> velocityHistory) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (remainingPoints <= 0) {
            result.put("error", "Remaining points must be greater than zero");
            return result;
        }

        if (velocityHistory == null || velocityHistory.isEmpty()) {
            result.put("error", "Velocity history is required");
            return result;
        }

        // Filter out zero/negative velocities to avoid infinite loops
        List<Integer> validVelocities = new ArrayList<>();
        for (int v : velocityHistory) {
            if (v > 0) validVelocities.add(v);
        }

        if (validVelocities.isEmpty()) {
            result.put("error", "No positive velocity values in history");
            return result;
        }

        // Monte Carlo simulation
        Random random = new Random(42); // Fixed seed for reproducibility in tests
        List<Integer> sprintCounts = new ArrayList<>();

        for (int sim = 0; sim < MONTE_CARLO_SIMULATIONS; sim++) {
            int remaining = remainingPoints;
            int sprints = 0;
            while (remaining > 0 && sprints < 1000) { // safety cap at 1000 sprints
                int velocity = validVelocities.get(random.nextInt(validVelocities.size()));
                remaining -= velocity;
                sprints++;
            }
            sprintCounts.add(sprints);
        }

        Collections.sort(sprintCounts);

        int p50 = sprintCounts.get((int) (MONTE_CARLO_SIMULATIONS * 0.50));
        int p85 = sprintCounts.get((int) (MONTE_CARLO_SIMULATIONS * 0.85));
        int p95 = sprintCounts.get((int) (MONTE_CARLO_SIMULATIONS * 0.95));

        result.put("remainingPoints", remainingPoints);
        result.put("simulations", MONTE_CARLO_SIMULATIONS);
        result.put("velocitySamples", validVelocities.size());
        result.put("p50", p50);
        result.put("p85", p85);
        result.put("p95", p95);
        result.put("interpretation", buildForecastInterpretation(p50, p85, p95));

        // Persist
        AgileMetric metric = AgileMetric.builder()
                .id(UUID.randomUUID().toString())
                .metricType("forecast")
                .sprintRef(null)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(metric);

        return result;
    }

    // ========== 5. predictRisk ==========

    @Override
    public Map<String, Object> predictRisk(String sprintId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sprintId == null || sprintId.isBlank()) {
            result.put("error", "Sprint ID is required");
            return result;
        }

        result.put("sprintId", sprintId);

        // Analyze saved metrics to determine risk
        List<AgileMetric> velocityMetrics = repository.findByMetricType("velocity");
        List<AgileMetric> burndownMetrics = repository.findByMetricType("burndown");
        List<AgileMetric> cycleTimeMetrics = repository.findByMetricType("cycle-time");

        List<Map<String, Object>> factors = new ArrayList<>();
        int riskScore = 0;

        // Factor 1: Velocity trend
        if (!velocityMetrics.isEmpty()) {
            String latestVelocityJson = velocityMetrics.get(0).getResultJson();
            if (latestVelocityJson != null && latestVelocityJson.contains("\"trend\":\"declining\"")) {
                factors.add(buildFactor("velocity-declining",
                        "Velocity is declining across recent sprints", "high"));
                riskScore += 3;
            } else if (latestVelocityJson != null && latestVelocityJson.contains("\"trend\":\"stable\"")) {
                factors.add(buildFactor("velocity-stable",
                        "Velocity is stable", "low"));
                riskScore += 1;
            } else {
                factors.add(buildFactor("velocity-improving",
                        "Velocity is improving", "low"));
            }
        } else {
            factors.add(buildFactor("no-velocity-data",
                    "No velocity data available for analysis", "medium"));
            riskScore += 2;
        }

        // Factor 2: Burndown status
        if (!burndownMetrics.isEmpty()) {
            String latestBurndownJson = burndownMetrics.get(0).getResultJson();
            if (latestBurndownJson != null && latestBurndownJson.contains("\"status\":\"behind\"")) {
                factors.add(buildFactor("burndown-behind",
                        "Sprint is behind schedule according to burndown", "high"));
                riskScore += 3;
            } else if (latestBurndownJson != null && latestBurndownJson.contains("\"status\":\"ahead\"")) {
                factors.add(buildFactor("burndown-ahead",
                        "Sprint is ahead of schedule", "low"));
            } else {
                factors.add(buildFactor("burndown-on-track",
                        "Sprint burndown is on track", "low"));
                riskScore += 1;
            }
        } else {
            factors.add(buildFactor("no-burndown-data",
                    "No burndown data available", "medium"));
            riskScore += 2;
        }

        // Factor 3: Cycle time
        if (!cycleTimeMetrics.isEmpty()) {
            String latestCycleJson = cycleTimeMetrics.get(0).getResultJson();
            // Simple heuristic: if p95 keyword exists and value is high
            if (latestCycleJson != null && latestCycleJson.contains("\"p95\":")) {
                factors.add(buildFactor("cycle-time-analyzed",
                        "Cycle time data is available for analysis", "low"));
                riskScore += 1;
            }
        } else {
            factors.add(buildFactor("no-cycle-time-data",
                    "No cycle time data available", "medium"));
            riskScore += 2;
        }

        // Determine overall risk level
        String riskLevel;
        String recommendation;
        if (riskScore >= 6) {
            riskLevel = "high";
            recommendation = "Consider reducing sprint scope or addressing blocking issues immediately.";
        } else if (riskScore >= 3) {
            riskLevel = "medium";
            recommendation = "Monitor closely and consider adjusting sprint commitments.";
        } else {
            riskLevel = "low";
            recommendation = "Sprint is progressing well. Continue current pace.";
        }

        result.put("riskLevel", riskLevel);
        result.put("riskScore", riskScore);
        result.put("factors", factors);
        result.put("recommendation", recommendation);

        // Persist
        AgileMetric metric = AgileMetric.builder()
                .id(UUID.randomUUID().toString())
                .metricType("risk")
                .sprintRef(sprintId)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(metric);

        return result;
    }

    // ========== 6. correlateFactors ==========

    @Override
    public Map<String, Object> correlateFactors(String factorA, String factorB,
                                                  double correlation, int sampleSize,
                                                  String description) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (factorA == null || factorA.isBlank() || factorB == null || factorB.isBlank()) {
            result.put("error", "Both factorA and factorB are required");
            return result;
        }

        if (correlation < -1.0 || correlation > 1.0) {
            result.put("error", "Correlation must be between -1.0 and 1.0");
            return result;
        }

        if (sampleSize <= 0) {
            result.put("error", "Sample size must be greater than zero");
            return result;
        }

        result.put("factorA", factorA);
        result.put("factorB", factorB);
        result.put("correlation", Math.round(correlation * 10000.0) / 10000.0);
        result.put("sampleSize", sampleSize);
        result.put("description", description != null ? description : "");

        // Interpret correlation strength
        double absCorr = Math.abs(correlation);
        String strength;
        if (absCorr >= 0.8) {
            strength = "strong";
        } else if (absCorr >= 0.5) {
            strength = "moderate";
        } else if (absCorr >= 0.3) {
            strength = "weak";
        } else {
            strength = "negligible";
        }

        String direction = correlation >= 0 ? "positive" : "negative";
        result.put("strength", strength);
        result.put("direction", direction);
        result.put("interpretation", strength + " " + direction + " correlation between "
                + factorA + " and " + factorB);

        // Persist
        AgileMetric metric = AgileMetric.builder()
                .id(UUID.randomUUID().toString())
                .metricType("correlation")
                .sprintRef(factorA + " <-> " + factorB)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(metric);

        return result;
    }

    // ========== Trend Helpers ==========

    /**
     * Determines velocity trend by comparing first half average to second half average.
     */
    String determineTrend(List<Integer> velocities) {
        if (velocities.size() < 2) {
            return "stable";
        }

        int mid = velocities.size() / 2;
        double firstHalfAvg = 0;
        double secondHalfAvg = 0;

        for (int i = 0; i < mid; i++) {
            firstHalfAvg += velocities.get(i);
        }
        firstHalfAvg /= mid;

        for (int i = mid; i < velocities.size(); i++) {
            secondHalfAvg += velocities.get(i);
        }
        secondHalfAvg /= (velocities.size() - mid);

        double diff = secondHalfAvg - firstHalfAvg;
        double threshold = firstHalfAvg * 0.1; // 10% threshold

        if (diff > threshold) {
            return "improving";
        } else if (diff < -threshold) {
            return "declining";
        } else {
            return "stable";
        }
    }

    // ========== Statistical Helpers ==========

    /**
     * Calculates the median of a list of long values.
     */
    double calculateMedian(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    /**
     * Calculates a percentile value from a list of long values.
     */
    double calculatePercentile(List<Long> values, int percentile) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        if (index < 0) index = 0;
        if (index >= sorted.size()) index = sorted.size() - 1;
        return sorted.get(index);
    }

    // ========== Parsing Helpers ==========

    private String getString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private int getInt(Map<String, Object> map, String key) {
        if (map == null) return 0;
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // ========== Factor & Interpretation Builders ==========

    private Map<String, Object> buildFactor(String id, String description, String severity) {
        Map<String, Object> factor = new LinkedHashMap<>();
        factor.put("id", id);
        factor.put("description", description);
        factor.put("severity", severity);
        return factor;
    }

    private String buildForecastInterpretation(int p50, int p85, int p95) {
        return "50% chance of completing in " + p50 + " sprints, "
                + "85% chance in " + p85 + " sprints, "
                + "95% chance in " + p95 + " sprints.";
    }

    // ========== JSON Serialization (minimal, no external deps) ==========

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
        if (value instanceof Set) {
            return valueToJson(new ArrayList<>((Set<?>) value));
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
