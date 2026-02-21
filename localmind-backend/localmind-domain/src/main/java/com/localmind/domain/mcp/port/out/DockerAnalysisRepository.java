package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.ComposeAnalysis;

/**
 * Output port for persisting Docker analysis results.
 */
public interface DockerAnalysisRepository {

    /**
     * Saves a compose/dockerfile analysis result.
     *
     * @param analysis the analysis to persist
     * @return the saved analysis
     */
    ComposeAnalysis save(ComposeAnalysis analysis);
}
