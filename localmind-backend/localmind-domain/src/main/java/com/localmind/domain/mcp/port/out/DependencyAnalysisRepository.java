package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.LicenseAudit;
import com.localmind.domain.mcp.model.VulnerabilityScan;

/**
 * Output port for persisting dependency analysis results.
 */
public interface DependencyAnalysisRepository {

    /**
     * Saves a vulnerability scan result.
     *
     * @param scan the scan result to persist
     * @return the saved scan (with generated id if new)
     */
    VulnerabilityScan saveVulnerabilityScan(VulnerabilityScan scan);

    /**
     * Saves a license audit result.
     *
     * @param audit the audit result to persist
     * @return the saved audit (with generated id if new)
     */
    LicenseAudit saveLicenseAudit(LicenseAudit audit);

    /**
     * Saves an unused dependency analysis result.
     * Stored as a VulnerabilityScan with projectType suffixed with "-unused".
     *
     * @param scan the analysis result to persist
     * @return the saved scan
     */
    VulnerabilityScan saveUnusedAnalysis(VulnerabilityScan scan);
}
