package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.WorkflowDefinition;
import com.localmind.domain.mcp.model.WorkflowRun;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting workflow definitions and workflow runs.
 */
public interface WorkflowRepository {

    /**
     * Saves a workflow definition.
     *
     * @param definition the workflow definition to persist
     * @return the saved workflow definition
     */
    WorkflowDefinition saveDefinition(WorkflowDefinition definition);

    /**
     * Finds a workflow definition by its identifier.
     *
     * @param id the workflow definition id
     * @return an Optional containing the definition if found
     */
    Optional<WorkflowDefinition> findDefinitionById(String id);

    /**
     * Returns all workflow definitions.
     *
     * @return list of all workflow definitions
     */
    List<WorkflowDefinition> findAllDefinitions();

    /**
     * Saves a workflow run.
     *
     * @param run the workflow run to persist
     * @return the saved workflow run
     */
    WorkflowRun saveRun(WorkflowRun run);

    /**
     * Finds a workflow run by its identifier.
     *
     * @param id the run id
     * @return an Optional containing the run if found
     */
    Optional<WorkflowRun> findRunById(String id);
}
