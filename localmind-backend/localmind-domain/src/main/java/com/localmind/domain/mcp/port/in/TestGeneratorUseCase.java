package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the test-generator tool group.
 * Provides unit test skeleton generation, edge case detection, and coverage analysis.
 */
public interface TestGeneratorUseCase {

    /**
     * Generates unit test skeletons from source code.
     *
     * @param code      the source code to generate tests for
     * @param language  programming language (java, typescript, javascript, python)
     * @param framework test framework (junit, vitest, pytest)
     * @return generated test result with language, framework, functionsFound, testsGenerated, testCode, functions
     */
    Map<String, Object> generateUnitTests(String code, String language, String framework);

    /**
     * Performs static analysis to identify edge cases in the given code.
     *
     * @param code the source code to analyze
     * @return edge case analysis with totalEdgeCases, byCategory, bySeverity, edgeCases list
     */
    Map<String, Object> findEdgeCases(String code);

    /**
     * Analyzes test coverage by matching function names between source and test code.
     *
     * @param sourceCode the source code containing functions
     * @param testCode   the test code to check for references
     * @return coverage analysis with totalFunctions, coveredFunctions, uncoveredFunctions, coveragePercentage, details
     */
    Map<String, Object> analyzeCoverage(String sourceCode, String testCode);
}
