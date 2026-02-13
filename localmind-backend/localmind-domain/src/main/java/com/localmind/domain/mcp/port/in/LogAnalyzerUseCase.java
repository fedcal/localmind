package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the log-analyzer tool group.
 * Provides log analysis, error pattern detection, tail functionality, and summary generation.
 */
public interface LogAnalyzerUseCase {

    /**
     * Analyzes a log file content: counts levels, extracts top errors, detects time range.
     *
     * @param content the log file content as a string
     * @param format  the log format: "auto", "json", "plain" (default "auto")
     * @return analysis result with totalLines, levels, topErrors, timeRange, format
     */
    Map<String, Object> analyzeLogFile(String content, String format);

    /**
     * Finds recurring error patterns by normalizing error messages and grouping by pattern.
     *
     * @param content  the log file content as a string
     * @param minCount minimum occurrences to include a pattern (default 2)
     * @return patterns result with totalPatternsFound, minCount, patterns[]
     */
    Map<String, Object> findErrorPatterns(String content, int minCount);

    /**
     * Returns the last N lines of log content, optionally filtered by a keyword.
     *
     * @param content the log file content as a string
     * @param lines   number of lines to return (default 50)
     * @param filter  optional case-insensitive filter keyword (null for no filter)
     * @return tail result with totalLines, returnedLines, filter, content
     */
    Map<String, Object> tailLog(String content, int lines, String filter);

    /**
     * Generates a human-readable summary of the log content.
     *
     * @param content the log file content as a string
     * @return summary result with summary, totalLines, levels, errorRate, warningRate
     */
    Map<String, Object> generateLogSummary(String content);
}
