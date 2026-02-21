package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.HttpRequestResult;

import java.util.List;

/**
 * Output port for persisting HTTP request results.
 */
public interface HttpRequestRepository {

    /**
     * Saves an HTTP request result.
     *
     * @param result the result to persist
     * @return the saved result (with generated id if new)
     */
    HttpRequestResult save(HttpRequestResult result);

    /**
     * Returns all persisted HTTP request results.
     *
     * @return list of all results
     */
    List<HttpRequestResult> findAll();
}
