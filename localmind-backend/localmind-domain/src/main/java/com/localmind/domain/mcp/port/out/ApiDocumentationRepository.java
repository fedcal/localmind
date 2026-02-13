package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.ApiDocumentation;

/**
 * Output port for persisting API documentation analysis results.
 */
public interface ApiDocumentationRepository {

    /**
     * Saves an API documentation analysis result.
     *
     * @param documentation the documentation analysis to persist
     * @return the saved documentation analysis
     */
    ApiDocumentation save(ApiDocumentation documentation);
}
