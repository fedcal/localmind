package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the code-review tool group.
 * Provides static analysis, complexity checking, and improvement suggestions.
 */
public interface CodeReviewUseCase {

    /**
     * Analyzes a git diff string for common issues (console.log, TODO, debugger, credentials, etc.).
     *
     * @param diff the git diff text to analyze
     * @return analysis result with stats, files, issues, and severity breakdown
     */
    Map<String, Object> analyzeDiff(String diff);

    /**
     * Calculates cyclomatic complexity of the given code.
     *
     * @param code     the source code to analyze
     * @param language the programming language (java, python, javascript, etc.)
     * @return complexity result with total, rating, breakdown, and line count
     */
    Map<String, Object> checkComplexity(String code, String language);

    /**
     * Suggests code improvements (magic numbers, long functions, deep nesting, duplicates, unused vars).
     *
     * @param code     the source code to analyze
     * @param language the programming language (java, python, javascript, etc.)
     * @return suggestions result with totals, types, severities, and detailed suggestions
     */
    Map<String, Object> suggestImprovements(String code, String language);
}
