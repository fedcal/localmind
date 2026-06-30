package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the workflow orchestrator tool group.
 * Provides workflow definition, execution, and management capabilities.
 */
public interface WorkflowOrchestratorUseCase {

    /**
     * Creates a new workflow definition.
     *
     * @param name         the workflow name
     * @param description  optional description
     * @param triggerEvent the event that triggers this workflow
     * @param stepsJson    JSON array of step definitions
     * @return result with id, name, triggerEvent, steps, active, createdAt
     */
    Map<String, Object> createWorkflow(String name, String description, String triggerEvent, String stepsJson);

    /**
     * Lists all workflow definitions with optional active filter.
     *
     * @param activeFilter optional filter: "true" or "false"
     * @return result with workflows list and count
     */
    Map<String, Object> listWorkflows(String activeFilter);

    /**
     * Triggers execution of a workflow.
     *
     * @param workflowId  the workflow id to trigger
     * @param payloadJson optional JSON payload for the run
     * @return result with runId, workflowId, status, stepResults, startedAt, completedAt
     */
    Map<String, Object> triggerWorkflow(String workflowId, String payloadJson);

    /**
     * Gets details of a specific workflow run.
     *
     * @param runId the run id
     * @return result with runId, workflowId, status, payload, stepResults, startedAt, completedAt
     */
    Map<String, Object> getWorkflowRun(String runId);

    /**
     * Toggles a workflow's active state.
     *
     * @param workflowId the workflow id
     * @param active     the new active state
     * @return result with workflowId, name, active
     */
    Map<String, Object> toggleWorkflow(String workflowId, boolean active);
}
