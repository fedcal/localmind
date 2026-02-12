package com.localmind.domain.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegexServiceTest {

    private RegexService regexService;

    @BeforeEach
    void setUp() {
        regexService = new RegexService();
    }

    // ======================== testRegex ========================

    @Test
    void testRegex_withValidPattern_returnsMatches() {
        var result = regexService.testRegex(
                "\\d+",
                List.of("123", "abc", "45x"),
                null
        );

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("pattern")).isEqualTo("\\d+");

        @SuppressWarnings("unchecked")
        var results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(3);

        // "123" should match
        assertThat(results.get(0).get("input")).isEqualTo("123");
        assertThat(results.get(0).get("matches")).isEqualTo(true);
        assertThat(results.get(0).get("fullMatch")).isEqualTo("123");

        // "abc" should not match
        assertThat(results.get(1).get("input")).isEqualTo("abc");
        assertThat(results.get(1).get("matches")).isEqualTo(false);
        assertThat(results.get(1).get("fullMatch")).isNull();

        // "45x" should not match (partial match, but matches() requires full match)
        assertThat(results.get(2).get("input")).isEqualTo("45x");
        assertThat(results.get(2).get("matches")).isEqualTo(false);
    }

    @Test
    void testRegex_withInvalidPattern_returnsError() {
        var result = regexService.testRegex(
                "[invalid",
                List.of("test"),
                null
        );

        assertThat(result.get("valid")).isEqualTo(false);
        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("Unclosed character class");

        @SuppressWarnings("unchecked")
        var results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).isEmpty();
    }

    @Test
    void testRegex_withFlags_appliesCorrectly() {
        // Case-insensitive flag
        var result = regexService.testRegex(
                "hello",
                List.of("HELLO", "hello", "Hello"),
                "i"
        );

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("flags")).isEqualTo("i");

        @SuppressWarnings("unchecked")
        var results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(3);

        // All should match with case-insensitive flag
        assertThat(results.get(0).get("matches")).isEqualTo(true);
        assertThat(results.get(1).get("matches")).isEqualTo(true);
        assertThat(results.get(2).get("matches")).isEqualTo(true);
    }

    @Test
    void testRegex_withGroups_extractsGroups() {
        var result = regexService.testRegex(
                "(\\d{4})-(\\d{2})-(\\d{2})",
                List.of("2024-01-15"),
                null
        );

        assertThat(result.get("valid")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        var results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(1);

        var first = results.get(0);
        assertThat(first.get("matches")).isEqualTo(true);
        assertThat(first.get("fullMatch")).isEqualTo("2024-01-15");

        @SuppressWarnings("unchecked")
        var groups = (List<String>) first.get("matchedGroups");
        assertThat(groups).containsExactly("2024", "01", "15");
    }

    // ======================== explainRegex ========================

    @Test
    void explainRegex_withSimplePattern_returnsExplanation() {
        var result = regexService.explainRegex("^\\d+$");

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("pattern")).isEqualTo("^\\d+$");

        @SuppressWarnings("unchecked")
        var components = (List<Map<String, String>>) result.get("components");
        assertThat(components).isNotEmpty();

        // Should contain start anchor, digit, quantifier, end anchor
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("component")).isEqualTo("^"));
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("explanation")).containsIgnoringCase("start"));
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("component")).isEqualTo("$"));
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("explanation")).containsIgnoringCase("end"));
    }

    @Test
    void explainRegex_withComplexPattern_returnsDetailedExplanation() {
        // Email-like pattern with multiple component types
        var result = regexService.explainRegex("(?:abc)\\w+[0-9](?=test)");

        assertThat(result.get("valid")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        var components = (List<Map<String, String>>) result.get("components");
        assertThat(components).isNotEmpty();

        // Should have non-capturing group, word char, character class, lookahead
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("explanation")).containsIgnoringCase("non-capturing"));
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("explanation")).containsIgnoringCase("word character"));
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("explanation")).containsIgnoringCase("character class"));
        assertThat(components).anySatisfy(comp ->
                assertThat(comp.get("explanation")).containsIgnoringCase("lookahead"));
    }

    // ======================== buildRegex ========================

    @Test
    void buildRegex_withEmail_returnsEmailPattern() {
        var result = regexService.buildRegex("email");

        assertThat(result.get("found")).isEqualTo(true);
        assertThat(result.get("type")).isEqualTo("email");
        assertThat((String) result.get("pattern")).isNotEmpty();

        // Verify the returned pattern actually matches a valid email
        var testResult = regexService.testRegex(
                (String) result.get("pattern"),
                List.of("user@example.com"),
                null
        );

        @SuppressWarnings("unchecked")
        var testResults = (List<Map<String, Object>>) testResult.get("results");
        assertThat(testResults.get(0).get("matches")).isEqualTo(true);
    }

    @Test
    void buildRegex_withUrl_returnsUrlPattern() {
        var result = regexService.buildRegex("url");

        assertThat(result.get("found")).isEqualTo(true);
        assertThat(result.get("type")).isEqualTo("url");
        assertThat((String) result.get("pattern")).isNotEmpty();

        // Verify the returned pattern actually matches a valid URL
        var testResult = regexService.testRegex(
                (String) result.get("pattern"),
                List.of("https://example.com"),
                null
        );

        @SuppressWarnings("unchecked")
        var testResults = (List<Map<String, Object>>) testResult.get("results");
        assertThat(testResults.get(0).get("matches")).isEqualTo(true);
    }

    @Test
    void buildRegex_withUnknown_returnsSuggestion() {
        var result = regexService.buildRegex("quantum entanglement detector");

        assertThat(result.get("found")).isEqualTo(false);
        assertThat(result.get("pattern")).isNull();
        assertThat((String) result.get("suggestion")).contains("No built-in pattern found");
        assertThat((String) result.get("suggestion")).contains("Available patterns:");
    }

    // ======================== optimizeRegex ========================

    @Test
    void optimizeRegex_withRedundantCharClass_suggestsSimplification() {
        var result = regexService.optimizeRegex("[0-9]+");

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("optimized")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        var suggestions = (List<Map<String, String>>) result.get("suggestions");
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).anySatisfy(s -> {
            assertThat(s.get("type")).isEqualTo("redundant-char-class");
            assertThat(s.get("original")).isEqualTo("[0-9]");
            assertThat(s.get("replacement")).isEqualTo("\\d");
        });
    }

    @Test
    void optimizeRegex_withUnnecessaryCaptureGroup_suggestsNonCapturing() {
        var result = regexService.optimizeRegex("(abc)(def)");

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("optimized")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        var suggestions = (List<Map<String, String>>) result.get("suggestions");
        assertThat(suggestions).anySatisfy(s -> {
            assertThat(s.get("type")).isEqualTo("unnecessary-capture-group");
            assertThat(s.get("description")).contains("capturing group");
            assertThat(s.get("replacement")).isEqualTo("(?:...)");
        });
    }

    @Test
    void optimizeRegex_withOptimalPattern_returnsNoSuggestions() {
        var result = regexService.optimizeRegex("^\\d{3}$");

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("optimized")).isEqualTo(false);

        @SuppressWarnings("unchecked")
        var suggestions = (List<Map<String, String>>) result.get("suggestions");
        assertThat(suggestions).isEmpty();
    }

    @Test
    void optimizeRegex_withInvalidPattern_returnsError() {
        var result = regexService.optimizeRegex("[unclosed");

        assertThat(result.get("valid")).isEqualTo(false);
        assertThat(result.get("error")).isNotNull();
        assertThat(result.get("optimized")).isEqualTo(false);
    }

    // ======================== convertRegex ========================

    @Test
    void convertRegex_toJavaScript_addsSlashes() {
        var result = regexService.convertRegex("\\d+", "javascript");

        assertThat(result.get("convertedPattern")).isEqualTo("/\\d+/");
        assertThat(result.get("targetFormat")).isEqualTo("javascript");

        @SuppressWarnings("unchecked")
        var notes = (List<String>) result.get("notes");
        assertThat(notes).isNotEmpty();
        assertThat(notes).anySatisfy(note ->
                assertThat(note).containsIgnoringCase("javascript"));
    }

    @Test
    void convertRegex_toPython_addsRawString() {
        var result = regexService.convertRegex("\\d+", "python");

        assertThat(result.get("convertedPattern")).isEqualTo("r'\\d+'");
        assertThat(result.get("targetFormat")).isEqualTo("python");

        @SuppressWarnings("unchecked")
        var notes = (List<String>) result.get("notes");
        assertThat(notes).isNotEmpty();
        assertThat(notes).anySatisfy(note ->
                assertThat(note).containsIgnoringCase("raw string"));
    }

    @Test
    void convertRegex_toJava_doublesBackslashes() {
        var result = regexService.convertRegex("\\d+\\w*", "java");

        assertThat(result.get("convertedPattern")).isEqualTo("\"\\\\d+\\\\w*\"");
        assertThat(result.get("targetFormat")).isEqualTo("java");

        @SuppressWarnings("unchecked")
        var notes = (List<String>) result.get("notes");
        assertThat(notes).isNotEmpty();
        assertThat(notes).anySatisfy(note ->
                assertThat(note).containsIgnoringCase("double-escaping"));
    }

    @Test
    void convertRegex_toPcre_addsSlashes() {
        var result = regexService.convertRegex("\\d+", "pcre");

        assertThat(result.get("convertedPattern")).isEqualTo("/\\d+/");
        assertThat(result.get("targetFormat")).isEqualTo("pcre");

        @SuppressWarnings("unchecked")
        var notes = (List<String>) result.get("notes");
        assertThat(notes).isNotEmpty();
        assertThat(notes).anySatisfy(note ->
                assertThat(note).containsIgnoringCase("PCRE"));
    }

    @Test
    void convertRegex_toUnknownFormat_returnsOriginalWithWarning() {
        var result = regexService.convertRegex("\\d+", "ruby");

        assertThat(result.get("convertedPattern")).isEqualTo("\\d+");

        @SuppressWarnings("unchecked")
        var notes = (List<String>) result.get("notes");
        assertThat(notes).anySatisfy(note ->
                assertThat(note).contains("Unknown format"));
    }
}
