package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the performance-profiler tool group.
 * Provides bundle analysis, bottleneck detection, and benchmark template generation.
 */
public interface PerformanceProfilerUseCase {

    /**
     * Analyzes source code for bundle size issues by parsing imports and detecting heavy dependencies.
     *
     * @param code     the source code to analyze (not read from disk)
     * @param filePath the file path for context (used in output, not for reading)
     * @return analysis result with imports, heavy dependencies, and size estimates
     */
    Map<String, Object> analyzeBundle(String code, String filePath);

    /**
     * Performs static analysis to find performance anti-patterns and bottlenecks.
     *
     * @param code     the source code to analyze
     * @param language the programming language (java, typescript, javascript, python)
     * @return bottleneck result with severity breakdown, type counts, and detailed findings
     */
    Map<String, Object> findBottlenecks(String code, String language);

    /**
     * Generates a benchmark comparison template ready to execute (does NOT execute code).
     *
     * @param codeA      the first code snippet to benchmark
     * @param codeB      the second code snippet to benchmark
     * @param iterations the number of benchmark iterations
     * @param language   the target language ("java" or "javascript", default: "java")
     * @return benchmark template with generated code and execution instructions
     */
    Map<String, Object> benchmarkCompare(String codeA, String codeB, int iterations, String language);
}
