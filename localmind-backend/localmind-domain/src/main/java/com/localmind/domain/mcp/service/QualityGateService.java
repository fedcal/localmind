package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.GateEvaluation;
import com.localmind.domain.mcp.model.QualityGate;
import com.localmind.domain.mcp.port.in.QualityGateUseCase;
import com.localmind.domain.mcp.port.out.QualityGateRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service implementing the quality-gate tool group.
 * Provides quality gate definition, evaluation against metrics, listing and history.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class QualityGateService implements QualityGateUseCase {

    private final QualityGateRepository repository;

    public QualityGateService(QualityGateRepository repository) {
        this.repository = repository;
    }

    // ========== 1. defineGate ==========

    @Override
    public Map<String, Object> defineGate(String name, String projectName, String checksJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (name == null || name.isBlank()) {
            result.put("error", "Gate name is required");
            return result;
        }

        if (checksJson == null || checksJson.isBlank()) {
            result.put("error", "Checks JSON is required");
            return result;
        }

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        QualityGate gate = QualityGate.builder()
                .id(id)
                .name(name)
                .projectName(projectName)
                .checksJson(checksJson)
                .createdAt(now)
                .build();

        repository.saveGate(gate);

        result.put("id", id);
        result.put("name", name);
        result.put("projectName", projectName);
        result.put("checks", parseChecksArray(checksJson));
        result.put("createdAt", now.toString());

        return result;
    }

    // ========== 2. evaluateGate ==========

    @Override
    public Map<String, Object> evaluateGate(String gateId, String metricsJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (gateId == null || gateId.isBlank()) {
            result.put("error", "Gate ID is required");
            return result;
        }

        if (metricsJson == null || metricsJson.isBlank()) {
            result.put("error", "Metrics JSON is required");
            return result;
        }

        Optional<QualityGate> gateOpt = repository.findGateById(gateId);
        if (gateOpt.isEmpty()) {
            result.put("error", "Gate not found: " + gateId);
            return result;
        }

        QualityGate gate = gateOpt.get();
        List<Map<String, Object>> checks = parseChecksArray(gate.getChecksJson());
        Map<String, Double> metrics = parseMetrics(metricsJson);

        List<Map<String, Object>> evaluationResults = new ArrayList<>();
        boolean allPassed = true;

        for (Map<String, Object> check : checks) {
            String metric = (String) check.get("metric");
            String operator = (String) check.get("operator");
            double threshold = toDouble(check.get("threshold"));

            Double actual = metrics.get(metric);
            boolean checkPassed = false;

            if (actual != null) {
                checkPassed = evaluateCheck(actual, operator, threshold);
            }

            if (!checkPassed) {
                allPassed = false;
            }

            Map<String, Object> checkResult = new LinkedHashMap<>();
            checkResult.put("metric", metric);
            checkResult.put("operator", operator);
            checkResult.put("threshold", threshold);
            checkResult.put("actual", actual != null ? actual : "N/A");
            checkResult.put("passed", checkPassed);
            evaluationResults.add(checkResult);
        }

        Instant now = Instant.now();
        String evalId = UUID.randomUUID().toString();

        GateEvaluation evaluation = GateEvaluation.builder()
                .id(evalId)
                .gateId(gateId)
                .passed(allPassed)
                .metricsJson(metricsJson)
                .resultsJson(resultsToJson(evaluationResults))
                .createdAt(now)
                .build();

        repository.saveEvaluation(evaluation);

        result.put("gateId", gateId);
        result.put("gateName", gate.getName());
        result.put("passed", allPassed);
        result.put("results", evaluationResults);
        result.put("evaluatedAt", now.toString());

        return result;
    }

    // ========== 3. listGates ==========

    @Override
    public Map<String, Object> listGates(String projectName) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<QualityGate> allGates = repository.findAllGates();

        List<QualityGate> filtered;
        if (projectName != null && !projectName.isBlank()) {
            filtered = new ArrayList<>();
            for (QualityGate g : allGates) {
                if (projectName.equals(g.getProjectName())) {
                    filtered.add(g);
                }
            }
        } else {
            filtered = allGates;
        }

        List<Map<String, Object>> gateList = new ArrayList<>();
        for (QualityGate g : filtered) {
            Map<String, Object> gateMap = new LinkedHashMap<>();
            gateMap.put("id", g.getId());
            gateMap.put("name", g.getName());
            gateMap.put("projectName", g.getProjectName());
            gateMap.put("checks", parseChecksArray(g.getChecksJson()));
            gateMap.put("createdAt", g.getCreatedAt().toString());
            gateList.add(gateMap);
        }

        result.put("gates", gateList);
        result.put("count", gateList.size());

        return result;
    }

    // ========== 4. getGateHistory ==========

    @Override
    public Map<String, Object> getGateHistory(String gateId, int limit) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (gateId == null || gateId.isBlank()) {
            result.put("error", "Gate ID is required");
            return result;
        }

        List<GateEvaluation> evaluations = repository.findEvaluationsByGateId(gateId);

        // Sort by createdAt descending
        evaluations.sort(Comparator.comparing(GateEvaluation::getCreatedAt).reversed());

        // Limit
        int effectiveLimit = limit > 0 ? limit : 10;
        List<GateEvaluation> limited = evaluations.size() > effectiveLimit
                ? evaluations.subList(0, effectiveLimit)
                : evaluations;

        List<Map<String, Object>> evalList = new ArrayList<>();
        for (GateEvaluation e : limited) {
            Map<String, Object> evalMap = new LinkedHashMap<>();
            evalMap.put("id", e.getId());
            evalMap.put("passed", e.isPassed());
            evalMap.put("metricsJson", e.getMetricsJson());
            evalMap.put("resultsJson", e.getResultsJson());
            evalMap.put("createdAt", e.getCreatedAt().toString());
            evalList.add(evalMap);
        }

        result.put("gateId", gateId);
        result.put("evaluations", evalList);
        result.put("count", evalList.size());

        return result;
    }

    // ========== JSON Parsing Helpers ==========

    /**
     * Parses a JSON array of check objects.
     * Expected format: [{"metric":"coverage","operator":">=","threshold":80}]
     */
    List<Map<String, Object>> parseChecksArray(String json) {
        List<Map<String, Object>> checks = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return checks;
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return checks;
        }

        // Remove outer brackets
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) {
            return checks;
        }

        // Split by },{ pattern
        List<String> objects = splitJsonObjects(inner);

        for (String obj : objects) {
            Map<String, Object> check = parseJsonObject(obj);
            if (!check.isEmpty()) {
                checks.add(check);
            }
        }

        return checks;
    }

    /**
     * Parses a JSON object mapping metric names to numeric values.
     * Expected format: {"coverage":85,"complexity":12}
     */
    Map<String, Double> parseMetrics(String json) {
        Map<String, Double> metrics = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return metrics;
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return metrics;
        }

        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) {
            return metrics;
        }

        String[] pairs = inner.split(",");
        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String key = pair.substring(0, colonIdx).trim().replace("\"", "");
                String value = pair.substring(colonIdx + 1).trim().replace("\"", "");
                try {
                    metrics.put(key, Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }

        return metrics;
    }

    /**
     * Evaluates a single check: actual {operator} threshold.
     */
    boolean evaluateCheck(double actual, String operator, double threshold) {
        if (operator == null) {
            return false;
        }
        return switch (operator) {
            case ">=" -> actual >= threshold;
            case "<=" -> actual <= threshold;
            case ">" -> actual > threshold;
            case "<" -> actual < threshold;
            case "==" -> Double.compare(actual, threshold) == 0;
            case "!=" -> Double.compare(actual, threshold) != 0;
            default -> false;
        };
    }

    /**
     * Splits a JSON string containing multiple objects separated by commas.
     */
    private List<String> splitJsonObjects(String inner) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    objects.add(inner.substring(start, i + 1).trim());
                    // Skip comma and whitespace after closing brace
                    start = i + 1;
                    while (start < inner.length() && (inner.charAt(start) == ',' || inner.charAt(start) == ' ')) {
                        start++;
                    }
                }
            }
        }

        return objects;
    }

    /**
     * Parses a single JSON object string into a map.
     */
    private Map<String, Object> parseJsonObject(String obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        String trimmed = obj.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return map;
        }

        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        String[] pairs = inner.split(",");

        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String key = pair.substring(0, colonIdx).trim().replace("\"", "");
                String value = pair.substring(colonIdx + 1).trim().replace("\"", "");
                // Try to parse as number
                try {
                    if (value.contains(".")) {
                        map.put(key, Double.parseDouble(value));
                    } else {
                        map.put(key, Double.parseDouble(value));
                    }
                } catch (NumberFormatException e) {
                    map.put(key, value);
                }
            }
        }

        return map;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Converts a list of result maps to a JSON string.
     */
    private String resultsToJson(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapToJson(results.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

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

    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
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
