package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.Sprint;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting Sprint entities.
 */
public interface SprintRepository {

    /**
     * Saves a sprint.
     *
     * @param sprint the sprint to persist
     * @return the saved sprint
     */
    Sprint save(Sprint sprint);

    /**
     * Finds a sprint by its id.
     *
     * @param id the sprint id
     * @return optional containing the sprint if found
     */
    Optional<Sprint> findById(String id);

    /**
     * Returns all sprints.
     *
     * @return list of all sprints
     */
    List<Sprint> findAll();
}
