package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Use case interface for regex operations.
 * Provides pattern testing, explanation, building, optimization and conversion capabilities.
 */
public interface RegexUseCase {

    /**
     * Tests a regex pattern against a list of test strings.
     *
     * @param pattern     the regex pattern to test
     * @param testStrings the strings to test against
     * @param flags       optional flags (e.g. "i" for case-insensitive, "m" for multiline)
     * @return results for each test string including match info and captured groups
     */
    Map<String, Object> testRegex(String pattern, List<String> testStrings, String flags);

    /**
     * Explains a regex pattern in natural language, component by component.
     *
     * @param pattern the regex pattern to explain
     * @return a map containing a list of component explanations
     */
    Map<String, Object> explainRegex(String pattern);

    /**
     * Builds a regex pattern from a natural language description or keyword.
     *
     * @param description the description or keyword (e.g. "email", "url", "uuid")
     * @return a map containing the generated pattern and description
     */
    Map<String, Object> buildRegex(String description);

    /**
     * Analyzes a regex pattern and suggests optimizations.
     *
     * @param pattern the regex pattern to optimize
     * @return a map containing optimization suggestions
     */
    Map<String, Object> optimizeRegex(String pattern);

    /**
     * Converts a regex pattern to the notation of a target language/format.
     *
     * @param pattern  the regex pattern to convert
     * @param toFormat the target format (java, python, javascript, pcre)
     * @return a map containing the converted pattern and notes
     */
    Map<String, Object> convertRegex(String pattern, String toFormat);
}
