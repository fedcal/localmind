package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.InsightResult;

import java.util.List;

/**
 * Output port for persisting insight analysis results.
 */
public interface InsightRepository {

    /**
     * Saves an insight result.
     *
     * @param insight the insight to persist
     * @return the saved insight
     */
    InsightResult save(InsightResult insight);

    /**
     * Returns all stored insights.
     *
     * @return list of all insight results
     */
    List<InsightResult> findAll();
}
