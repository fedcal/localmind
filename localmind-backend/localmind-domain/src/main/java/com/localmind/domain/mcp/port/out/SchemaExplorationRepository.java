package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.SchemaExploration;

/**
 * Output port for persisting schema exploration results.
 */
public interface SchemaExplorationRepository {

    /**
     * Saves a schema exploration result.
     *
     * @param exploration the result to persist
     * @return the saved result (with generated id if new)
     */
    SchemaExploration save(SchemaExploration exploration);
}
