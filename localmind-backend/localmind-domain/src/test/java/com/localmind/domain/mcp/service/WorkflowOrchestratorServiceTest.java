package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.WorkflowDefinition;
import com.localmind.domain.mcp.model.WorkflowRun;
import com.localmind.domain.mcp.port.out.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowOrchestratorServiceTest {

    @Mock
    private WorkflowRepository repository;

    private WorkflowOrchestratorService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowOrchestratorService(repository);
        when(repository.saveDefinition(any(WorkflowDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveRun(any(WorkflowRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== createWorkflow Tests ==========

    @Test
    void createWorkflow_withValidData_createsWorkflow() {
        Map<String, Object> result = service.createWorkflow("Deploy Pipeline",
                "Auto deploy", "push-to-main",
                "[{\"name\":\"build\"},{\"name\":\"test\"},{\"name\":\"deploy\"}]");

        assertThat(result.get("name")).isEqualTo("Deploy Pipeline");
        assertThat(result.get("triggerEvent")).isEqualTo("push-to-main");
        assertThat(result.get("active")).isEqualTo(true);
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
    }

    @Test
    void createWorkflow_withNullName_returnsError() {
        Map<String, Object> result = service.createWorkflow(null, "desc", "event", "[{}]");

        assertThat(result.get("error")).isEqualTo("Name is required");
        verify(repository, never()).saveDefinition(any());
    }

    @Test
    void createWorkflow_withBlankName_returnsError() {
        Map<String, Object> result = service.createWorkflow("  ", "desc", "event", "[{}]");

        assertThat(result.get("error")).isEqualTo("Name is required");
        verify(repository, never()).saveDefinition(any());
    }

    @Test
    void createWorkflow_withNullTriggerEvent_returnsError() {
        Map<String, Object> result = service.createWorkflow("name", "desc", null, "[{}]");

        assertThat(result.get("error")).isEqualTo("Trigger event is required");
        verify(repository, never()).saveDefinition(any());
    }

    @Test
    void createWorkflow_withBlankTriggerEvent_returnsError() {
        Map<String, Object> result = service.createWorkflow("name", "desc", "  ", "[{}]");

        assertThat(result.get("error")).isEqualTo("Trigger event is required");
        verify(repository, never()).saveDefinition(any());
    }

    @Test
    void createWorkflow_withNullStepsJson_returnsError() {
        Map<String, Object> result = service.createWorkflow("name", "desc", "event", null);

        assertThat(result.get("error")).isEqualTo("Steps JSON is required");
        verify(repository, never()).saveDefinition(any());
    }

    @Test
    void createWorkflow_withBlankStepsJson_returnsError() {
        Map<String, Object> result = service.createWorkflow("name", "desc", "event", "");

        assertThat(result.get("error")).isEqualTo("Steps JSON is required");
        verify(repository, never()).saveDefinition(any());
    }

    @Test
    void createWorkflow_savesDefinition() {
        service.createWorkflow("Build", null, "commit", "[{\"name\":\"compile\"}]");

        ArgumentCaptor<WorkflowDefinition> captor = ArgumentCaptor.forClass(WorkflowDefinition.class);
        verify(repository).saveDefinition(captor.capture());
        WorkflowDefinition saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Build");
        assertThat(saved.getTriggerEvent()).isEqualTo("commit");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getStepsJson()).isEqualTo("[{\"name\":\"compile\"}]");
    }

    // ========== listWorkflows Tests ==========

    @Test
    void listWorkflows_withNoFilter_returnsAll() {
        WorkflowDefinition w1 = buildWorkflowDefinition("wf-1", "Build", true);
        WorkflowDefinition w2 = buildWorkflowDefinition("wf-2", "Deploy", false);
        when(repository.findAllDefinitions()).thenReturn(List.of(w1, w2));

        Map<String, Object> result = service.listWorkflows(null);

        List<Map<String, Object>> workflows = (List<Map<String, Object>>) result.get("workflows");
        assertThat(workflows).hasSize(2);
        assertThat((int) result.get("count")).isEqualTo(2);
    }

    @Test
    void listWorkflows_withActiveFilter_filtersCorrectly() {
        WorkflowDefinition w1 = buildWorkflowDefinition("wf-1", "Build", true);
        WorkflowDefinition w2 = buildWorkflowDefinition("wf-2", "Deploy", false);
        when(repository.findAllDefinitions()).thenReturn(List.of(w1, w2));

        Map<String, Object> result = service.listWorkflows("true");

        List<Map<String, Object>> workflows = (List<Map<String, Object>>) result.get("workflows");
        assertThat(workflows).hasSize(1);
        assertThat(workflows.get(0).get("name")).isEqualTo("Build");
    }

    @Test
    void listWorkflows_withInactiveFilter_filtersCorrectly() {
        WorkflowDefinition w1 = buildWorkflowDefinition("wf-1", "Build", true);
        WorkflowDefinition w2 = buildWorkflowDefinition("wf-2", "Deploy", false);
        when(repository.findAllDefinitions()).thenReturn(List.of(w1, w2));

        Map<String, Object> result = service.listWorkflows("false");

        List<Map<String, Object>> workflows = (List<Map<String, Object>>) result.get("workflows");
        assertThat(workflows).hasSize(1);
        assertThat(workflows.get(0).get("name")).isEqualTo("Deploy");
    }

    // ========== triggerWorkflow Tests ==========

    @Test
    void triggerWorkflow_withActiveWorkflow_runsSuccessfully() {
        WorkflowDefinition def = WorkflowDefinition.builder()
                .id("wf-1").name("Build").triggerEvent("commit")
                .stepsJson("[{\"name\":\"compile\"},{\"name\":\"test\"}]")
                .active(true).createdAt(Instant.now()).build();
        when(repository.findDefinitionById("wf-1")).thenReturn(Optional.of(def));

        Map<String, Object> result = service.triggerWorkflow("wf-1", "{\"branch\":\"main\"}");

        assertThat(result.get("workflowId")).isEqualTo("wf-1");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("runId")).isNotNull();
        assertThat(result.get("startedAt")).isNotNull();
        assertThat(result.get("completedAt")).isNotNull();

        String stepResults = (String) result.get("stepResults");
        assertThat(stepResults).contains("stepIndex");
        assertThat(stepResults).contains("completed");
        assertThat(stepResults).contains("Simulated");
    }

    @Test
    void triggerWorkflow_withInactiveWorkflow_returnsError() {
        WorkflowDefinition def = WorkflowDefinition.builder()
                .id("wf-1").name("Build").triggerEvent("commit")
                .stepsJson("[{\"name\":\"compile\"}]")
                .active(false).createdAt(Instant.now()).build();
        when(repository.findDefinitionById("wf-1")).thenReturn(Optional.of(def));

        Map<String, Object> result = service.triggerWorkflow("wf-1", null);

        assertThat(result.get("error")).asString().contains("not active");
    }

    @Test
    void triggerWorkflow_withNullId_returnsError() {
        Map<String, Object> result = service.triggerWorkflow(null, null);

        assertThat(result.get("error")).isEqualTo("Workflow id is required");
    }

    @Test
    void triggerWorkflow_withNonExistentId_returnsError() {
        when(repository.findDefinitionById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.triggerWorkflow("non-existent", null);

        assertThat(result.get("error")).asString().contains("Workflow not found");
    }

    @Test
    void triggerWorkflow_savesRun() {
        WorkflowDefinition def = WorkflowDefinition.builder()
                .id("wf-1").name("Build").triggerEvent("commit")
                .stepsJson("[{\"name\":\"step1\"}]")
                .active(true).createdAt(Instant.now()).build();
        when(repository.findDefinitionById("wf-1")).thenReturn(Optional.of(def));

        service.triggerWorkflow("wf-1", "{\"key\":\"val\"}");

        ArgumentCaptor<WorkflowRun> captor = ArgumentCaptor.forClass(WorkflowRun.class);
        verify(repository).saveRun(captor.capture());
        WorkflowRun saved = captor.getValue();
        assertThat(saved.getWorkflowId()).isEqualTo("wf-1");
        assertThat(saved.getStatus()).isEqualTo("completed");
        assertThat(saved.getPayloadJson()).isEqualTo("{\"key\":\"val\"}");
        assertThat(saved.getStepResultsJson()).contains("completed");
    }

    // ========== getWorkflowRun Tests ==========

    @Test
    void getWorkflowRun_withValidId_returnsRunDetails() {
        WorkflowRun run = WorkflowRun.builder()
                .id("run-1").workflowId("wf-1").status("completed")
                .payloadJson("{\"branch\":\"main\"}")
                .stepResultsJson("[{\"stepIndex\":0,\"status\":\"completed\"}]")
                .startedAt(Instant.parse("2026-01-01T10:00:00Z"))
                .completedAt(Instant.parse("2026-01-01T10:05:00Z"))
                .build();
        when(repository.findRunById("run-1")).thenReturn(Optional.of(run));

        Map<String, Object> result = service.getWorkflowRun("run-1");

        assertThat(result.get("runId")).isEqualTo("run-1");
        assertThat(result.get("workflowId")).isEqualTo("wf-1");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("payload")).isEqualTo("{\"branch\":\"main\"}");
    }

    @Test
    void getWorkflowRun_withNullId_returnsError() {
        Map<String, Object> result = service.getWorkflowRun(null);

        assertThat(result.get("error")).isEqualTo("Run id is required");
    }

    @Test
    void getWorkflowRun_withNonExistentId_returnsError() {
        when(repository.findRunById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.getWorkflowRun("non-existent");

        assertThat(result.get("error")).asString().contains("Workflow run not found");
    }

    // ========== toggleWorkflow Tests ==========

    @Test
    void toggleWorkflow_withValidId_togglesActive() {
        WorkflowDefinition def = buildWorkflowDefinition("wf-1", "Build", true);
        when(repository.findDefinitionById("wf-1")).thenReturn(Optional.of(def));

        Map<String, Object> result = service.toggleWorkflow("wf-1", false);

        assertThat(result.get("workflowId")).isEqualTo("wf-1");
        assertThat(result.get("name")).isEqualTo("Build");
        assertThat(result.get("active")).isEqualTo(false);
    }

    @Test
    void toggleWorkflow_savesUpdatedDefinition() {
        WorkflowDefinition def = buildWorkflowDefinition("wf-1", "Build", true);
        when(repository.findDefinitionById("wf-1")).thenReturn(Optional.of(def));

        service.toggleWorkflow("wf-1", false);

        ArgumentCaptor<WorkflowDefinition> captor = ArgumentCaptor.forClass(WorkflowDefinition.class);
        verify(repository).saveDefinition(captor.capture());
        WorkflowDefinition saved = captor.getValue();
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getName()).isEqualTo("Build");
    }

    @Test
    void toggleWorkflow_withNullId_returnsError() {
        Map<String, Object> result = service.toggleWorkflow(null, true);

        assertThat(result.get("error")).isEqualTo("Workflow id is required");
    }

    @Test
    void toggleWorkflow_withNonExistentId_returnsError() {
        when(repository.findDefinitionById("non-existent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.toggleWorkflow("non-existent", true);

        assertThat(result.get("error")).asString().contains("Workflow not found");
    }

    // ========== Helper Methods Tests ==========

    @Test
    void simulateStepExecution_withMultipleSteps_generatesResults() {
        String stepsJson = "[{\"name\":\"build\"},{\"name\":\"test\"},{\"name\":\"deploy\"}]";
        String result = service.simulateStepExecution(stepsJson);

        assertThat(result).contains("\"stepIndex\":0");
        assertThat(result).contains("\"stepIndex\":1");
        assertThat(result).contains("\"stepIndex\":2");
        assertThat(result).contains("\"status\":\"completed\"");
        assertThat(result).contains("\"result\":\"Simulated\"");
    }

    @Test
    void simulateStepExecution_withEmptySteps_returnsEmptyArray() {
        assertThat(service.simulateStepExecution("[]")).isEqualTo("[]");
        assertThat(service.simulateStepExecution(null)).isEqualTo("[]");
        assertThat(service.simulateStepExecution("")).isEqualTo("[]");
    }

    @Test
    void countJsonArrayEntries_countsCorrectly() {
        assertThat(service.countJsonArrayEntries("[]")).isEqualTo(0);
        assertThat(service.countJsonArrayEntries("[{\"a\":1}]")).isEqualTo(1);
        assertThat(service.countJsonArrayEntries("[{\"a\":1},{\"b\":2}]")).isEqualTo(2);
        assertThat(service.countJsonArrayEntries(null)).isEqualTo(0);
    }

    // ========== Test Helpers ==========

    private WorkflowDefinition buildWorkflowDefinition(String id, String name, boolean active) {
        return WorkflowDefinition.builder()
                .id(id)
                .name(name)
                .triggerEvent("commit")
                .stepsJson("[{\"name\":\"step1\"}]")
                .active(active)
                .createdAt(Instant.now())
                .build();
    }
}
