package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.LogAnalysis;

/**
 * Output port for persisting log analysis results.
 */
public interface LogAnalysisRepository {

    /**
     * Saves a log analysis result.
     *
     * @param logAnalysis the result to persist
     * @return the saved result (with generated id if new)
     */
    LogAnalysis save(LogAnalysis logAnalysis);
}
