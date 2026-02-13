package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.LogAnalysis;
import com.localmind.domain.mcp.port.in.LogAnalyzerUseCase;
import com.localmind.domain.mcp.port.out.LogAnalysisRepository;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain service implementing the log-analyzer tool group.
 * Pure Java, zero Spring dependencies.
 */
public class LogAnalyzerService implements LogAnalyzerUseCase {

    private final LogAnalysisRepository logAnalysisRepository;

    private static final Pattern LEVEL_PATTERN = Pattern.compile(
            "\\b(ERROR|WARN(?:ING)?|INFO|DEBUG|TRACE|FATAL|CRITICAL)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d+)?(?:Z|[+-]\\d{2}:?\\d{2})?)" +
            "|(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2})" +
            "|(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})" +
            "|(\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2})");

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private static final Pattern HEX_PATTERN = Pattern.compile("\\b[0-9a-fA-F]{8,}\\b");

    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[^\\s\"']+");

    private static final Pattern PATH_PATTERN = Pattern.compile(
            "(?:/[\\w.\\-]+){2,}");

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile(
            "\"[^\"]*\"|'[^']*'");

    public LogAnalyzerService(LogAnalysisRepository logAnalysisRepository) {
        this.logAnalysisRepository = logAnalysisRepository;
    }

    // ======================== analyzeLogFile ========================

    @Override
    public Map<String, Object> analyzeLogFile(String content, String format) {
        if (content == null || content.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalLines", 0);
            empty.put("levels", Map.of());
            empty.put("topErrors", List.of());
            empty.put("timeRange", Map.of("earliest", "", "latest", ""));
            empty.put("format", format != null ? format : "auto");
            saveAnalysis(0, Map.of(), List.of());
            return empty;
        }

        String detectedFormat = resolveFormat(content, format);
        String[] lines = content.split("\n");

        Map<String, Integer> levels = new LinkedHashMap<>();
        Map<String, Integer> errorMessages = new LinkedHashMap<>();
        String earliest = null;
        String latest = null;

        for (String line : lines) {
            if (line.isBlank()) continue;

            ParsedLogLine parsed;
            if ("json".equals(detectedFormat)) {
                parsed = parseJsonLine(line);
            } else {
                parsed = parsePlainLine(line);
            }

            if (parsed.level != null) {
                String normalizedLevel = normalizeLevel(parsed.level);
                levels.merge(normalizedLevel, 1, Integer::sum);

                if ("ERROR".equals(normalizedLevel) || "FATAL".equals(normalizedLevel)
                        || "CRITICAL".equals(normalizedLevel)) {
                    String msg = parsed.message != null ? parsed.message.trim() : line.trim();
                    if (!msg.isEmpty()) {
                        errorMessages.merge(msg, 1, Integer::sum);
                    }
                }
            }

            if (parsed.timestamp != null) {
                if (earliest == null) {
                    earliest = parsed.timestamp;
                }
                latest = parsed.timestamp;
            }
        }

        // Top 10 error messages by frequency
        List<Map<String, Object>> topErrors = errorMessages.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("message", e.getKey());
                    entry.put("count", e.getValue());
                    return entry;
                })
                .toList();

        Map<String, Object> timeRange = new LinkedHashMap<>();
        timeRange.put("earliest", earliest != null ? earliest : "");
        timeRange.put("latest", latest != null ? latest : "");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLines", lines.length);
        result.put("levels", levels);
        result.put("topErrors", topErrors);
        result.put("timeRange", timeRange);
        result.put("format", detectedFormat);

        saveAnalysis(lines.length, levels, topErrors);
        return result;
    }

    // ======================== findErrorPatterns ========================

    @Override
    public Map<String, Object> findErrorPatterns(String content, int minCount) {
        if (minCount <= 0) {
            minCount = 2;
        }

        if (content == null || content.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalPatternsFound", 0);
            empty.put("minCount", minCount);
            empty.put("patterns", List.of());
            saveAnalysis(0, Map.of(), List.of());
            return empty;
        }

        String[] lines = content.split("\n");
        String detectedFormat = resolveFormat(content, null);

        // Collect error lines
        Map<String, List<String>> patternExamples = new LinkedHashMap<>();
        Map<String, Integer> patternCounts = new LinkedHashMap<>();

        for (String line : lines) {
            if (line.isBlank()) continue;

            ParsedLogLine parsed;
            if ("json".equals(detectedFormat)) {
                parsed = parseJsonLine(line);
            } else {
                parsed = parsePlainLine(line);
            }

            if (parsed.level != null) {
                String normalized = normalizeLevel(parsed.level);
                if ("ERROR".equals(normalized) || "FATAL".equals(normalized)
                        || "CRITICAL".equals(normalized)) {
                    String msg = parsed.message != null ? parsed.message.trim() : line.trim();
                    String pattern = normalizeErrorMessage(msg);

                    patternCounts.merge(pattern, 1, Integer::sum);
                    patternExamples.computeIfAbsent(pattern, k -> new ArrayList<>());
                    List<String> examples = patternExamples.get(pattern);
                    if (examples.size() < 3) {
                        examples.add(msg);
                    }
                }
            }
        }

        // Filter by minCount, sort desc
        int effectiveMinCount = minCount;
        List<Map<String, Object>> patterns = patternCounts.entrySet().stream()
                .filter(e -> e.getValue() >= effectiveMinCount)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("pattern", e.getKey());
                    entry.put("count", e.getValue());
                    entry.put("examples", patternExamples.getOrDefault(e.getKey(), List.of()));
                    return entry;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPatternsFound", patterns.size());
        result.put("minCount", effectiveMinCount);
        result.put("patterns", patterns);

        saveAnalysis(lines.length, Map.of(), List.of());
        return result;
    }

    // ======================== tailLog ========================

    @Override
    public Map<String, Object> tailLog(String content, int lines, String filter) {
        if (lines <= 0) {
            lines = 50;
        }

        if (content == null || content.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalLines", 0);
            empty.put("returnedLines", 0);
            empty.put("filter", filter);
            empty.put("content", "");
            return empty;
        }

        String[] allLines = content.split("\n");
        List<String> filtered;

        if (filter != null && !filter.isBlank()) {
            String lowerFilter = filter.toLowerCase();
            filtered = new ArrayList<>();
            for (String line : allLines) {
                if (line.toLowerCase().contains(lowerFilter)) {
                    filtered.add(line);
                }
            }
        } else {
            filtered = Arrays.asList(allLines);
        }

        int total = filtered.size();
        int start = Math.max(0, total - lines);
        List<String> tail = filtered.subList(start, total);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLines", allLines.length);
        result.put("returnedLines", tail.size());
        result.put("filter", filter);
        result.put("content", String.join("\n", tail));
        return result;
    }

    // ======================== generateLogSummary ========================

    @Override
    public Map<String, Object> generateLogSummary(String content) {
        if (content == null || content.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("summary", "No log content provided.");
            empty.put("totalLines", 0);
            empty.put("levels", Map.of());
            empty.put("errorRate", 0.0);
            empty.put("warningRate", 0.0);
            saveAnalysis(0, Map.of(), List.of());
            return empty;
        }

        // Reuse analyzeLogFile logic
        Map<String, Object> analysis = analyzeLogFileInternal(content);

        int totalLines = (int) analysis.get("totalLines");
        @SuppressWarnings("unchecked")
        Map<String, Integer> levels = (Map<String, Integer>) analysis.get("levels");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topErrors = (List<Map<String, Object>>) analysis.get("topErrors");
        @SuppressWarnings("unchecked")
        Map<String, Object> timeRange = (Map<String, Object>) analysis.get("timeRange");

        int errorCount = levels.getOrDefault("ERROR", 0)
                + levels.getOrDefault("FATAL", 0)
                + levels.getOrDefault("CRITICAL", 0);
        int warnCount = levels.getOrDefault("WARN", 0);

        double errorRate = totalLines > 0 ? (errorCount * 100.0) / totalLines : 0.0;
        double warningRate = totalLines > 0 ? (warnCount * 100.0) / totalLines : 0.0;

        // Round to 2 decimal places
        errorRate = Math.round(errorRate * 100.0) / 100.0;
        warningRate = Math.round(warningRate * 100.0) / 100.0;

        StringBuilder summary = new StringBuilder();
        summary.append("Log Analysis Summary\n");
        summary.append("====================\n");
        summary.append("Total lines: ").append(totalLines).append("\n\n");

        // Level breakdown with percentages
        summary.append("Level breakdown:\n");
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            double pct = totalLines > 0 ? (entry.getValue() * 100.0) / totalLines : 0.0;
            pct = Math.round(pct * 100.0) / 100.0;
            summary.append("  ").append(entry.getKey()).append(": ").append(entry.getValue())
                    .append(" (").append(pct).append("%)\n");
        }

        summary.append("\nError rate: ").append(errorRate).append("%\n");
        summary.append("Warning rate: ").append(warningRate).append("%\n");

        // Time range
        String earliest = (String) timeRange.get("earliest");
        String latest = (String) timeRange.get("latest");
        if (!earliest.isEmpty() && !latest.isEmpty()) {
            summary.append("\nTime range: ").append(earliest).append(" to ").append(latest).append("\n");
        }

        // Top 5 error messages
        if (!topErrors.isEmpty()) {
            summary.append("\nTop error messages:\n");
            int limit = Math.min(5, topErrors.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> err = topErrors.get(i);
                String msg = (String) err.get("message");
                if (msg.length() > 100) {
                    msg = msg.substring(0, 100) + "...";
                }
                summary.append("  ").append(i + 1).append(". [").append(err.get("count"))
                        .append("x] ").append(msg).append("\n");
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary.toString());
        result.put("totalLines", totalLines);
        result.put("levels", levels);
        result.put("errorRate", errorRate);
        result.put("warningRate", warningRate);

        saveAnalysis(totalLines, levels, topErrors);
        return result;
    }

    // ======================== Internal helpers ========================

    /**
     * Internal version of analyzeLogFile that does NOT persist to repository.
     * Used by generateLogSummary to avoid double-saving.
     */
    private Map<String, Object> analyzeLogFileInternal(String content) {
        String detectedFormat = resolveFormat(content, null);
        String[] lines = content.split("\n");

        Map<String, Integer> levels = new LinkedHashMap<>();
        Map<String, Integer> errorMessages = new LinkedHashMap<>();
        String earliest = null;
        String latest = null;

        for (String line : lines) {
            if (line.isBlank()) continue;

            ParsedLogLine parsed;
            if ("json".equals(detectedFormat)) {
                parsed = parseJsonLine(line);
            } else {
                parsed = parsePlainLine(line);
            }

            if (parsed.level != null) {
                String normalizedLevel = normalizeLevel(parsed.level);
                levels.merge(normalizedLevel, 1, Integer::sum);

                if ("ERROR".equals(normalizedLevel) || "FATAL".equals(normalizedLevel)
                        || "CRITICAL".equals(normalizedLevel)) {
                    String msg = parsed.message != null ? parsed.message.trim() : line.trim();
                    if (!msg.isEmpty()) {
                        errorMessages.merge(msg, 1, Integer::sum);
                    }
                }
            }

            if (parsed.timestamp != null) {
                if (earliest == null) {
                    earliest = parsed.timestamp;
                }
                latest = parsed.timestamp;
            }
        }

        List<Map<String, Object>> topErrors = errorMessages.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("message", e.getKey());
                    entry.put("count", e.getValue());
                    return entry;
                })
                .toList();

        Map<String, Object> timeRange = new LinkedHashMap<>();
        timeRange.put("earliest", earliest != null ? earliest : "");
        timeRange.put("latest", latest != null ? latest : "");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLines", lines.length);
        result.put("levels", levels);
        result.put("topErrors", topErrors);
        result.put("timeRange", timeRange);
        result.put("format", detectedFormat);
        return result;
    }

    private String resolveFormat(String content, String format) {
        if (format != null && !"auto".equalsIgnoreCase(format)) {
            return format.toLowerCase();
        }
        // Auto-detect: check if >50% of the first 10 non-blank lines look like JSON
        String[] lines = content.split("\n");
        int checked = 0;
        int jsonCount = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            if (checked >= 10) break;
            checked++;
            String trimmed = line.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                jsonCount++;
            }
        }
        return (checked > 0 && jsonCount > checked / 2) ? "json" : "plain";
    }

    private ParsedLogLine parseJsonLine(String line) {
        ParsedLogLine result = new ParsedLogLine();
        String trimmed = line.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return parsePlainLine(line);
        }

        // Simple JSON field extraction without a JSON library
        result.level = extractJsonField(trimmed, "level");
        if (result.level == null) result.level = extractJsonField(trimmed, "severity");
        if (result.level == null) result.level = extractJsonField(trimmed, "log_level");

        result.timestamp = extractJsonField(trimmed, "timestamp");
        if (result.timestamp == null) result.timestamp = extractJsonField(trimmed, "time");
        if (result.timestamp == null) result.timestamp = extractJsonField(trimmed, "@timestamp");

        result.message = extractJsonField(trimmed, "message");
        if (result.message == null) result.message = extractJsonField(trimmed, "msg");
        if (result.message == null) result.message = extractJsonField(trimmed, "error");

        return result;
    }

    private String extractJsonField(String json, String field) {
        // Matches "field" : "value" or "field": "value"
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private ParsedLogLine parsePlainLine(String line) {
        ParsedLogLine result = new ParsedLogLine();

        Matcher levelMatcher = LEVEL_PATTERN.matcher(line);
        if (levelMatcher.find()) {
            result.level = levelMatcher.group(1).toUpperCase();
        }

        Matcher tsMatcher = TIMESTAMP_PATTERN.matcher(line);
        if (tsMatcher.find()) {
            for (int i = 1; i <= tsMatcher.groupCount(); i++) {
                if (tsMatcher.group(i) != null) {
                    result.timestamp = tsMatcher.group(i);
                    break;
                }
            }
        }

        // For plain format, message is the line content after level (if found)
        if (result.level != null) {
            int levelEnd = levelMatcher.end();
            if (levelEnd < line.length()) {
                String after = line.substring(levelEnd).trim();
                // Strip leading separators like -, :, |
                if (!after.isEmpty() && (after.charAt(0) == '-' || after.charAt(0) == ':'
                        || after.charAt(0) == '|')) {
                    after = after.substring(1).trim();
                }
                result.message = after;
            }
        }

        return result;
    }

    private String normalizeLevel(String level) {
        if (level == null) return "UNKNOWN";
        String upper = level.toUpperCase();
        if ("WARNING".equals(upper)) return "WARN";
        return upper;
    }

    private String normalizeErrorMessage(String message) {
        if (message == null) return "";
        String normalized = message;

        // Order matters: replace more specific patterns first
        normalized = TIMESTAMP_PATTERN.matcher(normalized).replaceAll("<TIMESTAMP>");
        normalized = UUID_PATTERN.matcher(normalized).replaceAll("<UUID>");
        normalized = URL_PATTERN.matcher(normalized).replaceAll("<URL>");
        normalized = IP_PATTERN.matcher(normalized).replaceAll("<IP>");
        normalized = PATH_PATTERN.matcher(normalized).replaceAll("<PATH>");
        normalized = HEX_PATTERN.matcher(normalized).replaceAll("<HEX>");
        normalized = QUOTED_STRING_PATTERN.matcher(normalized).replaceAll("\"<STR>\"");
        normalized = NUMBER_PATTERN.matcher(normalized).replaceAll("<NUM>");

        return normalized;
    }

    private void saveAnalysis(int totalLines, Map<String, Integer> levels,
                               List<Map<String, Object>> topErrors) {
        LogAnalysis analysis = LogAnalysis.builder()
                .id(UUID.randomUUID().toString())
                .totalLines(totalLines)
                .levelsJson(levels.toString())
                .topErrorsJson(topErrors.toString())
                .createdAt(Instant.now())
                .build();
        logAnalysisRepository.save(analysis);
    }

    private static class ParsedLogLine {
        String level;
        String timestamp;
        String message;
    }
}
