package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.GeneratedTest;
import com.localmind.domain.mcp.port.in.TestGeneratorUseCase;
import com.localmind.domain.mcp.port.out.TestGeneratorRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain service implementing the test-generator tool group.
 * Pure Java, zero Spring dependencies.
 */
public class TestGeneratorService implements TestGeneratorUseCase {

    private final TestGeneratorRepository testGeneratorRepository;

    public TestGeneratorService(TestGeneratorRepository testGeneratorRepository) {
        this.testGeneratorRepository = testGeneratorRepository;
    }

    // --- Regex patterns for function extraction per language ---

    private static final Pattern JAVA_METHOD_PATTERN = Pattern.compile(
            "(public|protected|private)?\\s*(static)?\\s*\\w+\\s+(\\w+)\\s*\\("
    );

    private static final Pattern TS_JS_FUNCTION_PATTERN = Pattern.compile(
            "(export\\s+)?(async\\s+)?function\\s+(\\w+)"
    );

    private static final Pattern TS_JS_ARROW_PATTERN = Pattern.compile(
            "(export\\s+)?const\\s+(\\w+)\\s*=\\s*(\\(|async)"
    );

    private static final Pattern PYTHON_DEF_PATTERN = Pattern.compile(
            "def\\s+(\\w+)\\s*\\("
    );

    // --- Edge case detection patterns ---

    private static final Pattern STRING_OPS_PATTERN = Pattern.compile(
            "\\.(trim|split|substring|replace|indexOf|contains)\\s*\\("
    );

    private static final Pattern COLLECTION_OPS_PATTERN = Pattern.compile(
            "\\.(get|add|remove|size|stream|map|filter)\\s*\\("
    );

    private static final Pattern NUMERIC_OPS_PATTERN = Pattern.compile(
            "(\\+|-|\\*|/|parseInt|parseFloat|Integer\\.parseInt|Double\\.parseDouble|Long\\.parseLong)"
    );

    private static final Pattern DIVISION_PATTERN = Pattern.compile(
            "\\s/\\s|/=|\\bdiv\\b"
    );

    private static final Pattern ASYNC_PATTERN = Pattern.compile(
            "(async|Promise|CompletableFuture|Future<|CompletionStage)"
    );

    private static final Pattern TRY_CATCH_PATTERN = Pattern.compile(
            "(try\\s*\\{|catch\\s*\\()"
    );

    private static final Pattern NULL_CHECK_PATTERN = Pattern.compile(
            "(\\?\\.|\\.\\?|!=\\s*null|==\\s*null|Optional|isPresent|ifPresent|orElse)"
    );

    private static final Pattern REGEX_PATTERN = Pattern.compile(
            "(Pattern\\.compile|\\.(matches|replaceAll|replaceFirst)\\s*\\()"
    );

    private static final Pattern FILE_IO_PATTERN = Pattern.compile(
            "(Files\\.|BufferedReader|InputStream|OutputStream|FileReader|FileWriter|FileInputStream|FileOutputStream)"
    );

    @Override
    public Map<String, Object> generateUnitTests(String code, String language, String framework) {
        String resolvedLanguage = resolveLanguage(language);
        String resolvedFramework = resolveFramework(framework, resolvedLanguage);

        List<Map<String, Object>> functions = extractFunctions(code, resolvedLanguage);
        String testCode = generateTestCode(functions, resolvedLanguage, resolvedFramework);
        int testsGenerated = functions.size() * 3; // 3 test per funzione

        // Salva nel repository
        GeneratedTest generatedTest = GeneratedTest.builder()
                .id(UUID.randomUUID().toString())
                .sourceFilePath(null)
                .language(resolvedLanguage)
                .framework(resolvedFramework)
                .functionsFound(functions.size())
                .testCode(testCode)
                .createdAt(Instant.now())
                .build();
        testGeneratorRepository.save(generatedTest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("language", resolvedLanguage);
        result.put("framework", resolvedFramework);
        result.put("functionsFound", functions.size());
        result.put("testsGenerated", testsGenerated);
        result.put("testCode", testCode);
        result.put("functions", functions);
        return result;
    }

    @Override
    public Map<String, Object> findEdgeCases(String code) {
        List<Map<String, Object>> edgeCases = new ArrayList<>();

        // Rileva parametri funzione -> null/undefined
        detectFunctionParameters(code, edgeCases);

        // String operations
        if (STRING_OPS_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "string", "Empty string input", "Pass \"\" to string operation", "high");
            addEdgeCase(edgeCases, "string", "Whitespace-only string", "Pass \"   \" to string operation", "medium");
            addEdgeCase(edgeCases, "string", "Special characters in string", "Pass string with special chars (!@#$%)", "medium");
        }

        // Array/List operations
        if (COLLECTION_OPS_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "collection", "Empty collection", "Pass empty list/array", "high");
            addEdgeCase(edgeCases, "collection", "Single element collection", "Pass collection with 1 element", "medium");
            addEdgeCase(edgeCases, "collection", "Large collection", "Pass collection with 10000+ elements", "low");
        }

        // Numeric operations
        if (NUMERIC_OPS_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "numeric", "Zero value", "Pass 0 as numeric input", "high");
            addEdgeCase(edgeCases, "numeric", "Negative value", "Pass negative number", "high");
            addEdgeCase(edgeCases, "numeric", "Boundary values (MAX_VALUE/MIN_VALUE/NaN)", "Test with Integer.MAX_VALUE, MIN_VALUE, or NaN", "medium");
        }

        // Division
        if (DIVISION_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "numeric", "Division by zero", "Pass 0 as divisor", "high");
        }

        // Async/Promise
        if (ASYNC_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "async", "Rejection/exception in async operation", "Force async rejection or exception", "high");
            addEdgeCase(edgeCases, "async", "Timeout in async operation", "Simulate timeout scenario", "medium");
        }

        // Try/catch
        if (TRY_CATCH_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "error-handling", "Error propagation", "Verify errors propagate correctly through catch blocks", "high");
        }

        // Null check patterns
        if (NULL_CHECK_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "null-safety", "Deep nested null", "Pass object with nested null properties", "medium");
        }

        // Regex patterns
        if (REGEX_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "regex", "Special regex characters in input", "Pass input with [, ], (, ), *, +, ? characters", "medium");
        }

        // File I/O
        if (FILE_IO_PATTERN.matcher(code).find()) {
            addEdgeCase(edgeCases, "file-io", "File not found", "Pass non-existent file path", "high");
            addEdgeCase(edgeCases, "file-io", "Permission denied", "Pass file path without read/write permissions", "medium");
        }

        // Costruisci risultato
        Map<String, Integer> byCategory = new HashMap<>();
        Map<String, Integer> bySeverity = new HashMap<>();
        bySeverity.put("high", 0);
        bySeverity.put("medium", 0);
        bySeverity.put("low", 0);

        for (Map<String, Object> edgeCase : edgeCases) {
            String category = (String) edgeCase.get("category");
            String severity = (String) edgeCase.get("severity");
            byCategory.merge(category, 1, Integer::sum);
            bySeverity.merge(severity, 1, Integer::sum);
        }

        // Salva nel repository
        GeneratedTest generatedTest = GeneratedTest.builder()
                .id(UUID.randomUUID().toString())
                .sourceFilePath(null)
                .language("unknown")
                .framework("edge-case-analysis")
                .functionsFound(edgeCases.size())
                .testCode(edgeCases.toString())
                .createdAt(Instant.now())
                .build();
        testGeneratorRepository.save(generatedTest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEdgeCases", edgeCases.size());
        result.put("byCategory", byCategory);
        result.put("bySeverity", bySeverity);
        result.put("edgeCases", edgeCases);
        return result;
    }

    @Override
    public Map<String, Object> analyzeCoverage(String sourceCode, String testCode) {
        // Estrai funzioni dal sourceCode (multi-language: proviamo tutti i pattern)
        List<String> functionNames = extractAllFunctionNames(sourceCode);

        List<String> coveredFunctions = new ArrayList<>();
        List<String> uncoveredFunctions = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (String functionName : functionNames) {
            List<String> testMatches = findTestMatches(functionName, testCode);
            boolean hasCoverage = !testMatches.isEmpty();

            if (hasCoverage) {
                coveredFunctions.add(functionName);
            } else {
                uncoveredFunctions.add(functionName);
            }

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("functionName", functionName);
            detail.put("hasCoverage", hasCoverage);
            detail.put("testMatches", testMatches);
            details.add(detail);
        }

        int totalFunctions = functionNames.size();
        int coveragePercentage = totalFunctions == 0 ? 0
                : Math.round((float) coveredFunctions.size() / totalFunctions * 100);

        // Salva nel repository
        GeneratedTest generatedTest = GeneratedTest.builder()
                .id(UUID.randomUUID().toString())
                .sourceFilePath(null)
                .language("unknown")
                .framework("coverage-analysis")
                .functionsFound(totalFunctions)
                .testCode("covered=" + coveredFunctions.size() + "/" + totalFunctions)
                .createdAt(Instant.now())
                .build();
        testGeneratorRepository.save(generatedTest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalFunctions", totalFunctions);
        result.put("coveredFunctions", coveredFunctions);
        result.put("uncoveredFunctions", uncoveredFunctions);
        result.put("coveragePercentage", coveragePercentage);
        result.put("details", details);
        return result;
    }

    // ======================== Helper methods ========================

    String resolveLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "java";
        }
        return language.toLowerCase().trim();
    }

    String resolveFramework(String framework, String language) {
        if (framework != null && !framework.isBlank()) {
            return framework.toLowerCase().trim();
        }
        switch (language) {
            case "typescript":
            case "javascript":
                return "vitest";
            case "python":
                return "pytest";
            case "java":
            default:
                return "junit";
        }
    }

    List<Map<String, Object>> extractFunctions(String code, String language) {
        List<Map<String, Object>> functions = new ArrayList<>();

        switch (language) {
            case "java":
                extractJavaFunctions(code, functions);
                break;
            case "typescript":
            case "javascript":
                extractTsJsFunctions(code, functions);
                break;
            case "python":
                extractPythonFunctions(code, functions);
                break;
            default:
                extractJavaFunctions(code, functions);
                break;
        }

        return functions;
    }

    private void extractJavaFunctions(String code, List<Map<String, Object>> functions) {
        Matcher matcher = JAVA_METHOD_PATTERN.matcher(code);
        while (matcher.find()) {
            String name = matcher.group(3);
            // Escludi costruttori e parole chiave comuni
            if (isJavaKeyword(name)) {
                continue;
            }
            Map<String, Object> fn = new LinkedHashMap<>();
            fn.put("name", name);
            fn.put("parameters", extractParametersFromPosition(code, matcher.end()));
            fn.put("isAsync", false);
            fn.put("isExported", "public".equals(matcher.group(1)));
            functions.add(fn);
        }
    }

    private void extractTsJsFunctions(String code, List<Map<String, Object>> functions) {
        // function declarations
        Matcher fnMatcher = TS_JS_FUNCTION_PATTERN.matcher(code);
        while (fnMatcher.find()) {
            Map<String, Object> fn = new LinkedHashMap<>();
            fn.put("name", fnMatcher.group(3));
            fn.put("parameters", "");
            fn.put("isAsync", fnMatcher.group(2) != null);
            fn.put("isExported", fnMatcher.group(1) != null);
            functions.add(fn);
        }

        // arrow functions
        Matcher arrowMatcher = TS_JS_ARROW_PATTERN.matcher(code);
        while (arrowMatcher.find()) {
            Map<String, Object> fn = new LinkedHashMap<>();
            fn.put("name", arrowMatcher.group(2));
            fn.put("parameters", "");
            fn.put("isAsync", "async".equals(arrowMatcher.group(3)));
            fn.put("isExported", arrowMatcher.group(1) != null);
            functions.add(fn);
        }
    }

    private void extractPythonFunctions(String code, List<Map<String, Object>> functions) {
        Matcher matcher = PYTHON_DEF_PATTERN.matcher(code);
        while (matcher.find()) {
            String name = matcher.group(1);
            Map<String, Object> fn = new LinkedHashMap<>();
            fn.put("name", name);
            fn.put("parameters", "");
            fn.put("isAsync", false);
            fn.put("isExported", !name.startsWith("_"));
            functions.add(fn);
        }
    }

    private boolean isJavaKeyword(String name) {
        return "if".equals(name) || "for".equals(name) || "while".equals(name)
                || "switch".equals(name) || "catch".equals(name) || "return".equals(name)
                || "new".equals(name) || "class".equals(name) || "interface".equals(name)
                || "enum".equals(name) || "try".equals(name) || "throw".equals(name)
                || "synchronized".equals(name) || "assert".equals(name);
    }

    private String extractParametersFromPosition(String code, int position) {
        if (position >= code.length()) {
            return "";
        }
        int end = code.indexOf(')', position);
        if (end == -1) {
            return "";
        }
        return code.substring(position, end).trim();
    }

    String generateTestCode(List<Map<String, Object>> functions, String language, String framework) {
        StringBuilder sb = new StringBuilder();

        switch (framework) {
            case "junit":
                generateJUnitCode(sb, functions);
                break;
            case "vitest":
                generateVitestCode(sb, functions);
                break;
            case "pytest":
                generatePytestCode(sb, functions);
                break;
            default:
                generateJUnitCode(sb, functions);
                break;
        }

        return sb.toString();
    }

    private void generateJUnitCode(StringBuilder sb, List<Map<String, Object>> functions) {
        sb.append("import org.junit.jupiter.api.Test;\n");
        sb.append("import static org.assertj.core.api.Assertions.assertThat;\n\n");
        sb.append("class GeneratedTest {\n\n");

        for (Map<String, Object> fn : functions) {
            String name = (String) fn.get("name");
            String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);

            sb.append("    @Test\n");
            sb.append("    void ").append(name).append("_shouldExistAndBeCallable() {\n");
            sb.append("        // TODO: Verify ").append(name).append(" exists and is callable\n");
            sb.append("        assertThat(true).isTrue();\n");
            sb.append("    }\n\n");

            sb.append("    @Test\n");
            sb.append("    void ").append(name).append("_shouldReturnExpectedResultWithValidInput() {\n");
            sb.append("        // TODO: Test ").append(name).append(" with valid input\n");
            sb.append("        assertThat(true).isTrue();\n");
            sb.append("    }\n\n");

            sb.append("    @Test\n");
            sb.append("    void ").append(name).append("_shouldHandleEdgeCases() {\n");
            sb.append("        // TODO: Test edge cases for ").append(name).append("\n");
            sb.append("        assertThat(true).isTrue();\n");
            sb.append("    }\n\n");
        }

        sb.append("}\n");
    }

    private void generateVitestCode(StringBuilder sb, List<Map<String, Object>> functions) {
        sb.append("import { describe, it, expect } from 'vitest';\n\n");

        for (Map<String, Object> fn : functions) {
            String name = (String) fn.get("name");

            sb.append("describe('").append(name).append("', () => {\n");

            sb.append("    it('should exist and be callable', () => {\n");
            sb.append("        // TODO: Verify ").append(name).append(" exists and is callable\n");
            sb.append("        expect(true).toBe(true);\n");
            sb.append("    });\n\n");

            sb.append("    it('should return expected result with valid input', () => {\n");
            sb.append("        // TODO: Test ").append(name).append(" with valid input\n");
            sb.append("        expect(true).toBe(true);\n");
            sb.append("    });\n\n");

            sb.append("    it('should handle edge cases', () => {\n");
            sb.append("        // TODO: Test edge cases for ").append(name).append("\n");
            sb.append("        expect(true).toBe(true);\n");
            sb.append("    });\n");

            sb.append("});\n\n");
        }
    }

    private void generatePytestCode(StringBuilder sb, List<Map<String, Object>> functions) {
        sb.append("import pytest\n\n");

        for (Map<String, Object> fn : functions) {
            String name = (String) fn.get("name");

            sb.append("class Test").append(name.substring(0, 1).toUpperCase()).append(name.substring(1)).append(":\n\n");

            sb.append("    def test_should_exist_and_be_callable(self):\n");
            sb.append("        # TODO: Verify ").append(name).append(" exists and is callable\n");
            sb.append("        assert True\n\n");

            sb.append("    def test_should_return_expected_result_with_valid_input(self):\n");
            sb.append("        # TODO: Test ").append(name).append(" with valid input\n");
            sb.append("        assert True\n\n");

            sb.append("    def test_should_handle_edge_cases(self):\n");
            sb.append("        # TODO: Test edge cases for ").append(name).append("\n");
            sb.append("        assert True\n\n");
        }
    }

    private void detectFunctionParameters(String code, List<Map<String, Object>> edgeCases) {
        // Cerca funzioni con parametri in qualsiasi linguaggio
        Pattern paramPattern = Pattern.compile("(\\w+)\\s*\\([^)]+\\)");
        Matcher matcher = paramPattern.matcher(code);
        boolean hasParams = false;
        while (matcher.find()) {
            String params = code.substring(matcher.start(), matcher.end());
            if (params.contains(",") || params.matches(".*\\w+\\s+\\w+.*")) {
                hasParams = true;
                break;
            }
        }
        if (hasParams) {
            addEdgeCase(edgeCases, "null-safety", "Null/undefined parameter", "Pass null/undefined as function argument", "high");
        }
    }

    private void addEdgeCase(List<Map<String, Object>> edgeCases, String category,
                              String description, String example, String severity) {
        Map<String, Object> edgeCase = new LinkedHashMap<>();
        edgeCase.put("category", category);
        edgeCase.put("description", description);
        edgeCase.put("example", example);
        edgeCase.put("severity", severity);
        edgeCases.add(edgeCase);
    }

    List<String> extractAllFunctionNames(String sourceCode) {
        List<String> names = new ArrayList<>();

        // Java
        Matcher javaMatcher = JAVA_METHOD_PATTERN.matcher(sourceCode);
        while (javaMatcher.find()) {
            String name = javaMatcher.group(3);
            if (!isJavaKeyword(name) && !names.contains(name)) {
                names.add(name);
            }
        }

        // TS/JS function
        Matcher tsMatcher = TS_JS_FUNCTION_PATTERN.matcher(sourceCode);
        while (tsMatcher.find()) {
            String name = tsMatcher.group(3);
            if (!names.contains(name)) {
                names.add(name);
            }
        }

        // TS/JS arrow
        Matcher arrowMatcher = TS_JS_ARROW_PATTERN.matcher(sourceCode);
        while (arrowMatcher.find()) {
            String name = arrowMatcher.group(2);
            if (!names.contains(name)) {
                names.add(name);
            }
        }

        // Python
        Matcher pyMatcher = PYTHON_DEF_PATTERN.matcher(sourceCode);
        while (pyMatcher.find()) {
            String name = pyMatcher.group(1);
            if (!names.contains(name)) {
                names.add(name);
            }
        }

        return names;
    }

    List<String> findTestMatches(String functionName, String testCode) {
        List<String> matches = new ArrayList<>();

        // describe/class block
        Pattern describePattern = Pattern.compile("(describe|class)\\s*[('\"].*" + Pattern.quote(functionName) + ".*['\")]?");
        if (describePattern.matcher(testCode).find()) {
            matches.add("describe/class block");
        }

        // it/test/should block
        Pattern itPattern = Pattern.compile("(it|test|should)\\s*[('\"].*" + Pattern.quote(functionName) + ".*['\")]?");
        if (itPattern.matcher(testCode).find()) {
            matches.add("it/test/should block");
        }

        // Direct call
        Pattern callPattern = Pattern.compile(Pattern.quote(functionName) + "\\s*\\(");
        if (callPattern.matcher(testCode).find()) {
            matches.add("direct call");
        }

        return matches;
    }
}
