package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import com.localmind.domain.mcp.port.in.WorkflowOrchestratorUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindOpsTools {

    private final IncidentManagerUseCase incidentManagerUseCase;
    private final WorkflowOrchestratorUseCase workflowOrchestratorUseCase;

    public LocalMindOpsTools(IncidentManagerUseCase incidentManagerUseCase,
                              WorkflowOrchestratorUseCase workflowOrchestratorUseCase) {
        this.incidentManagerUseCase = incidentManagerUseCase;
        this.workflowOrchestratorUseCase = workflowOrchestratorUseCase;
    }

    // ==================== INCIDENT MANAGER ====================

    @Tool(description = "Open a new incident with title, severity and description. "
            + "Severity must be one of: critical, high, medium, low. "
            + "Returns the created incident with id, title, severity, status, description, createdAt.")
    public Map<String, Object> openIncident(
            @ToolParam(description = "Incident title") String title,
            @ToolParam(description = "Severity level: critical, high, medium, low") String severity,
            @ToolParam(description = "Description of the incident") String description,
            @ToolParam(description = "Optional JSON string listing affected systems") String affectedSystemsJson) {
        return incidentManagerUseCase.openIncident(title, severity, description, affectedSystemsJson);
    }

    @Tool(description = "Update an existing incident's status and/or add a note to its timeline. "
            + "Valid statuses: open, investigating, mitigating, resolved, postmortem. "
            + "Returns the updated incident with id, status, updated flag, timelineEntries count.")
    public Map<String, Object> updateIncident(
            @ToolParam(description = "The incident ID") String id,
            @ToolParam(description = "New status (optional)") String status,
            @ToolParam(description = "Note to add to timeline (optional)") String note) {
        return incidentManagerUseCase.updateIncident(id, status, note);
    }

    @Tool(description = "Add a timeline entry to an existing incident. "
            + "Returns the incidentId, entry details (timestamp, description, source), and totalEntries.")
    public Map<String, Object> addTimelineEntry(
            @ToolParam(description = "The incident ID") String incidentId,
            @ToolParam(description = "Description of the timeline event") String description,
            @ToolParam(description = "Source of the entry (e.g. 'monitoring', 'oncall', 'user')") String source) {
        return incidentManagerUseCase.addTimelineEntry(incidentId, description, source);
    }

    @Tool(description = "Resolve an incident, recording the resolution and root cause. "
            + "Sets status to 'resolved', records resolvedAt timestamp. "
            + "Returns id, status, resolution, rootCause, resolvedAt, duration.")
    public Map<String, Object> resolveIncident(
            @ToolParam(description = "The incident ID") String id,
            @ToolParam(description = "Description of how the incident was resolved") String resolution,
            @ToolParam(description = "Root cause analysis") String rootCause) {
        return incidentManagerUseCase.resolveIncident(id, resolution, rootCause);
    }

    @Tool(description = "Generate a post-mortem report in Markdown format for an incident. "
            + "Includes incident details, timeline, resolution, root cause, and suggested action items. "
            + "Returns incidentId, postmortem markdown text, generatedAt.")
    public Map<String, Object> generatePostmortem(
            @ToolParam(description = "The incident ID") String id) {
        return incidentManagerUseCase.generatePostmortem(id);
    }

    @Tool(description = "List incidents with optional filters for status and severity. "
            + "Returns incidents list with count and applied filters.")
    public Map<String, Object> listIncidents(
            @ToolParam(description = "Filter by status (optional): open, investigating, mitigating, resolved, postmortem") String status,
            @ToolParam(description = "Filter by severity (optional): critical, high, medium, low") String severity,
            @ToolParam(description = "Maximum number of results (default 50)") int limit) {
        return incidentManagerUseCase.listIncidents(status, severity, limit);
    }

    // ==================== WORKFLOW ORCHESTRATOR ====================

    @Tool(description = "Create a new workflow definition with a trigger event and steps. "
            + "The workflow is active by default. "
            + "Returns id, name, triggerEvent, steps, active, createdAt.")
    public Map<String, Object> createWorkflow(
            @ToolParam(description = "Workflow name") String name,
            @ToolParam(description = "Optional description") String description,
            @ToolParam(description = "Event that triggers this workflow") String triggerEvent,
            @ToolParam(description = "JSON array of step definitions") String stepsJson) {
        return workflowOrchestratorUseCase.createWorkflow(name, description, triggerEvent, stepsJson);
    }

    @Tool(description = "List all workflow definitions with optional active/inactive filter. "
            + "Returns workflows list and count.")
    public Map<String, Object> listWorkflows(
            @ToolParam(description = "Filter by active state: 'true' or 'false' (optional)") String activeFilter) {
        return workflowOrchestratorUseCase.listWorkflows(activeFilter);
    }

    @Tool(description = "Trigger execution of a workflow. The workflow must be active. "
            + "Simulates step execution and returns run results with runId, status, stepResults.")
    public Map<String, Object> triggerWorkflow(
            @ToolParam(description = "The workflow ID to trigger") String workflowId,
            @ToolParam(description = "Optional JSON payload for the run") String payloadJson) {
        return workflowOrchestratorUseCase.triggerWorkflow(workflowId, payloadJson);
    }

    @Tool(description = "Get details of a specific workflow run. "
            + "Returns runId, workflowId, status, payload, stepResults, startedAt, completedAt.")
    public Map<String, Object> getWorkflowRun(
            @ToolParam(description = "The workflow run ID") String runId) {
        return workflowOrchestratorUseCase.getWorkflowRun(runId);
    }

    @Tool(description = "Toggle a workflow's active/inactive state. "
            + "Returns workflowId, name, and new active state.")
    public Map<String, Object> toggleWorkflow(
            @ToolParam(description = "The workflow ID") String workflowId,
            @ToolParam(description = "New active state: true or false") boolean active) {
        return workflowOrchestratorUseCase.toggleWorkflow(workflowId, active);
    }
}
