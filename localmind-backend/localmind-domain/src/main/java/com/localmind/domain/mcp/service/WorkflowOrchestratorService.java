package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.WorkflowDefinition;
import com.localmind.domain.mcp.model.WorkflowRun;
import com.localmind.domain.mcp.port.in.WorkflowOrchestratorUseCase;
import com.localmind.domain.mcp.port.out.WorkflowRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service implementing the workflow orchestrator tool group.
 * Provides workflow creation, listing, triggering, run inspection and toggling.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class WorkflowOrchestratorService implements WorkflowOrchestratorUseCase {

    private final WorkflowRepository repository;

    public WorkflowOrchestratorService(WorkflowRepository repository) {
        this.repository = repository;
    }

    // ========== 1. createWorkflow ==========

    @Override
    public Map<String, Object> createWorkflow(String name, String description, String triggerEvent, String stepsJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (name == null || name.isBlank()) {
            result.put("error", "Name is required");
            return result;
        }
        if (triggerEvent == null || triggerEvent.isBlank()) {
            result.put("error", "Trigger event is required");
            return result;
        }
        if (stepsJson == null || stepsJson.isBlank()) {
            result.put("error", "Steps JSON is required");
            return result;
        }

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        WorkflowDefinition definition = WorkflowDefinition.builder()
                .id(id)
                .name(name)
                .description(description)
                .triggerEvent(triggerEvent)
                .stepsJson(stepsJson)
                .active(true)
                .createdAt(now)
                .build();

        repository.saveDefinition(definition);

        result.put("id", id);
        result.put("name", name);
        result.put("triggerEvent", triggerEvent);
        result.put("steps", stepsJson);
        result.put("active", true);
        result.put("createdAt", now.toString());

        return result;
    }

    // ========== 2. listWorkflows ==========

    @Override
    public Map<String, Object> listWorkflows(String activeFilter) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<WorkflowDefinition> all = repository.findAllDefinitions();

        // Filter by active if provided
        if (activeFilter != null && !activeFilter.isBlank()) {
            boolean filterActive = "true".equalsIgnoreCase(activeFilter.trim());
            all = all.stream()
                    .filter(w -> w.isActive() == filterActive)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> workflows = new ArrayList<>();
        for (WorkflowDefinition def : all) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", def.getId());
            item.put("name", def.getName());
            item.put("description", def.getDescription());
            item.put("triggerEvent", def.getTriggerEvent());
            item.put("active", def.isActive());
            item.put("createdAt", def.getCreatedAt() != null ? def.getCreatedAt().toString() : null);
            workflows.add(item);
        }

        result.put("workflows", workflows);
        result.put("count", workflows.size());

        return result;
    }

    // ========== 3. triggerWorkflow ==========

    @Override
    public Map<String, Object> triggerWorkflow(String workflowId, String payloadJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (workflowId == null || workflowId.isBlank()) {
            result.put("error", "Workflow id is required");
            return result;
        }

        Optional<WorkflowDefinition> optDef = repository.findDefinitionById(workflowId);
        if (optDef.isEmpty()) {
            result.put("error", "Workflow not found: " + workflowId);
            return result;
        }

        WorkflowDefinition definition = optDef.get();
        if (!definition.isActive()) {
            result.put("error", "Workflow is not active: " + workflowId);
            return result;
        }

        Instant startedAt = Instant.now();
        String runId = UUID.randomUUID().toString();

        // Simulate step execution: parse stepsJson and produce a result for each step
        String stepResultsJson = simulateStepExecution(definition.getStepsJson());

        Instant completedAt = Instant.now();

        WorkflowRun run = WorkflowRun.builder()
                .id(runId)
                .workflowId(workflowId)
                .status("completed")
                .payloadJson(payloadJson)
                .stepResultsJson(stepResultsJson)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();

        repository.saveRun(run);

        result.put("runId", runId);
        result.put("workflowId", workflowId);
        result.put("status", "completed");
        result.put("stepResults", stepResultsJson);
        result.put("startedAt", startedAt.toString());
        result.put("completedAt", completedAt.toString());

        return result;
    }

    // ========== 4. getWorkflowRun ==========

    @Override
    public Map<String, Object> getWorkflowRun(String runId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (runId == null || runId.isBlank()) {
            result.put("error", "Run id is required");
            return result;
        }

        Optional<WorkflowRun> optRun = repository.findRunById(runId);
        if (optRun.isEmpty()) {
            result.put("error", "Workflow run not found: " + runId);
            return result;
        }

        WorkflowRun run = optRun.get();

        result.put("runId", run.getId());
        result.put("workflowId", run.getWorkflowId());
        result.put("status", run.getStatus());
        result.put("payload", run.getPayloadJson());
        result.put("stepResults", run.getStepResultsJson());
        result.put("startedAt", run.getStartedAt() != null ? run.getStartedAt().toString() : null);
        result.put("completedAt", run.getCompletedAt() != null ? run.getCompletedAt().toString() : null);

        return result;
    }

    // ========== 5. toggleWorkflow ==========

    @Override
    public Map<String, Object> toggleWorkflow(String workflowId, boolean active) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (workflowId == null || workflowId.isBlank()) {
            result.put("error", "Workflow id is required");
            return result;
        }

        Optional<WorkflowDefinition> optDef = repository.findDefinitionById(workflowId);
        if (optDef.isEmpty()) {
            result.put("error", "Workflow not found: " + workflowId);
            return result;
        }

        WorkflowDefinition definition = optDef.get();

        WorkflowDefinition updatedDef = WorkflowDefinition.builder()
                .id(definition.getId())
                .name(definition.getName())
                .description(definition.getDescription())
                .triggerEvent(definition.getTriggerEvent())
                .triggerConditionsJson(definition.getTriggerConditionsJson())
                .stepsJson(definition.getStepsJson())
                .active(active)
                .createdAt(definition.getCreatedAt())
                .build();

        repository.saveDefinition(updatedDef);

        result.put("workflowId", workflowId);
        result.put("name", definition.getName());
        result.put("active", active);

        return result;
    }

    // ========== Helpers ==========

    /**
     * Simulates step execution by parsing the stepsJson array and generating
     * a step result for each step found.
     */
    String simulateStepExecution(String stepsJson) {
        if (stepsJson == null || stepsJson.isBlank()) {
            return "[]";
        }

        // Count the number of steps by counting top-level objects
        int stepCount = countJsonArrayEntries(stepsJson);
        if (stepCount == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < stepCount; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"stepIndex\":").append(i)
                    .append(",\"status\":\"completed\"")
                    .append(",\"result\":\"Simulated\"}");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Counts entries in a JSON array by tracking brace depth.
     */
    int countJsonArrayEntries(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank() || "[]".equals(jsonArray.trim())) {
            return 0;
        }
        String trimmed = jsonArray.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return 0;
        }
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) {
            return 0;
        }

        int count = 0;
        int depth = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    count++;
                }
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return count;
    }
}
