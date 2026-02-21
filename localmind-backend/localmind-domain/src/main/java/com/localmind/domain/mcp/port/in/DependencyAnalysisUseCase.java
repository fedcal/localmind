package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the dependency-manager tool group.
 * Provides vulnerability scanning, unused dependency detection, and license auditing.
 */
public interface DependencyAnalysisUseCase {

    /**
     * Scans project dependencies for known vulnerabilities.
     *
     * @param projectPath path to the project root directory
     * @param projectType "maven" or "npm"
     * @return analysis result containing vulnerabilities found
     */
    Map<String, Object> checkVulnerabilities(String projectPath, String projectType);

    /**
     * Finds dependencies declared but not used in source code.
     *
     * @param projectPath path to the project root directory
     * @param projectType "maven" or "npm"
     * @return analysis result containing unused dependencies
     */
    Map<String, Object> findUnusedDependencies(String projectPath, String projectType);

    /**
     * Audits project dependencies for copyleft or restrictive licenses.
     *
     * @param projectPath path to the project root directory
     * @param projectType "maven" or "npm"
     * @return audit result containing license information and warnings
     */
    Map<String, Object> licenseAudit(String projectPath, String projectType);
}
