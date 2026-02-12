package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.PerformanceProfilerUseCase;
import com.localmind.domain.mcp.port.in.TestGeneratorUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindTestTools {

    private final TestGeneratorUseCase testGeneratorUseCase;
    private final PerformanceProfilerUseCase performanceProfilerUseCase;

    public LocalMindTestTools(TestGeneratorUseCase testGeneratorUseCase,
                              PerformanceProfilerUseCase performanceProfilerUseCase) {
        this.testGeneratorUseCase = testGeneratorUseCase;
        this.performanceProfilerUseCase = performanceProfilerUseCase;
    }

    // ==================== TEST GENERATOR ====================

    @Tool(description = "Generate unit test skeletons from source code. Extracts functions/methods and creates test templates for JUnit (Java), Vitest (TS/JS), or Pytest (Python).")
    public Map<String, Object> generateUnitTests(
            @ToolParam(description = "The source code to generate tests for") String code,
            @ToolParam(description = "Programming language: java, typescript, javascript, python (default: java)") String language,
            @ToolParam(description = "Test framework: junit, vitest, pytest (default: based on language)") String framework) {
        return testGeneratorUseCase.generateUnitTests(code, language, framework);
    }

    @Tool(description = "Analyze code to identify edge cases for testing: null/empty values, boundary conditions, division by zero, async errors, file I/O failures, and more.")
    public Map<String, Object> findEdgeCases(
            @ToolParam(description = "The source code to analyze for edge cases") String code) {
        return testGeneratorUseCase.findEdgeCases(code);
    }

    @Tool(description = "Analyze test coverage by matching function names between source code and test code. Returns coverage percentage and details on covered/uncovered functions.")
    public Map<String, Object> analyzeCoverage(
            @ToolParam(description = "The source code containing functions to check") String sourceCode,
            @ToolParam(description = "The test code to check for function references") String testCode) {
        return testGeneratorUseCase.analyzeCoverage(sourceCode, testCode);
    }

    // ==================== PERFORMANCE PROFILER ====================

    @Tool(description = "Analyze source code imports for bundle size impact. Detects heavy dependencies like moment.js (300KB), lodash (70KB), aws-sdk (100MB+) and suggests alternatives.")
    public Map<String, Object> analyzeBundle(
            @ToolParam(description = "The source code to analyze") String code,
            @ToolParam(description = "File path for context (e.g. 'src/app.ts' or 'App.java')") String filePath) {
        return performanceProfilerUseCase.analyzeBundle(code, filePath);
    }

    @Tool(description = "Static analysis for performance anti-patterns: nested loops (O(n^2)), sync I/O, linear search in loops, missing pagination, DOM queries in loops, string concat in loops, JSON in loops, sequential await, and more.")
    public Map<String, Object> findBottlenecks(
            @ToolParam(description = "The source code to analyze") String code,
            @ToolParam(description = "Programming language: java, typescript, javascript, python") String language) {
        return performanceProfilerUseCase.findBottlenecks(code, language);
    }

    @Tool(description = "Generate a ready-to-run benchmark comparison template for two code snippets. Includes warmup, measurement, and statistical analysis (mean, median, p95, p99, ops/sec). Does NOT execute the code.")
    public Map<String, Object> benchmarkCompare(
            @ToolParam(description = "First code snippet to benchmark") String codeA,
            @ToolParam(description = "Second code snippet to benchmark") String codeB,
            @ToolParam(description = "Number of iterations (default: 1000)") int iterations,
            @ToolParam(description = "Target language: java, javascript (default: java)") String language) {
        return performanceProfilerUseCase.benchmarkCompare(codeA, codeB, iterations, language);
    }
}
