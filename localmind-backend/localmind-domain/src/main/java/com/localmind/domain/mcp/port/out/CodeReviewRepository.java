package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.CodeReviewResult;

/**
 * Output port for persisting code review results.
 */
public interface CodeReviewRepository {

    /**
     * Saves a code review result.
     *
     * @param result the result to persist
     * @return the saved result (with generated id if new)
     */
    CodeReviewResult save(CodeReviewResult result);
}
