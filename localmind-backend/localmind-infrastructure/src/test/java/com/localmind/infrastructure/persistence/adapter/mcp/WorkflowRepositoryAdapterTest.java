package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.WorkflowDefinition;
import com.localmind.domain.mcp.model.WorkflowRun;
import com.localmind.infrastructure.persistence.entity.mcp.WorkflowDefinitionEntity;
import com.localmind.infrastructure.persistence.entity.mcp.WorkflowRunEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaWorkflowDefinitionRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaWorkflowRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowRepositoryAdapterTest {

    @Mock
    private JpaWorkflowDefinitionRepository definitionJpaRepository;

    @Mock
    private JpaWorkflowRunRepository runJpaRepository;

    private WorkflowRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final UUID TEST_UUID_2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");
    private final Instant COMPLETED = Instant.parse("2026-02-13T10:05:00Z");

    @BeforeEach
    void setUp() {
        adapter = new WorkflowRepositoryAdapter(definitionJpaRepository, runJpaRepository);
    }

    // --- WorkflowDefinition Tests ---

    @Test
    void saveDefinition_mapsDomainToEntityAndBack() {
        WorkflowDefinition domain = WorkflowDefinition.builder()
                .id(TEST_UUID.toString())
                .name("Deploy Pipeline")
                .description("Auto deploy on merge")
                .triggerEvent("push-to-main")
                .triggerConditionsJson("{\"branch\":\"main\"}")
                .stepsJson("[{\"name\":\"build\"},{\"name\":\"deploy\"}]")
                .active(true)
                .createdAt(NOW)
                .build();

        WorkflowDefinitionEntity savedEntity = WorkflowDefinitionEntity.builder()
                .id(TEST_UUID)
                .name("Deploy Pipeline")
                .description("Auto deploy on merge")
                .triggerEvent("push-to-main")
                .triggerConditionsJson("{\"branch\":\"main\"}")
                .stepsJson("[{\"name\":\"build\"},{\"name\":\"deploy\"}]")
                .active(true)
                .createdAt(NOW)
                .build();

        when(definitionJpaRepository.save(any(WorkflowDefinitionEntity.class))).thenReturn(savedEntity);

        WorkflowDefinition result = adapter.saveDefinition(domain);

        ArgumentCaptor<WorkflowDefinitionEntity> captor = ArgumentCaptor.forClass(WorkflowDefinitionEntity.class);
        verify(definitionJpaRepository).save(captor.capture());

        WorkflowDefinitionEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("Deploy Pipeline");
        assertThat(captured.getTriggerEvent()).isEqualTo("push-to-main");
        assertThat(captured.isActive()).isTrue();
        assertThat(captured.getStepsJson()).isEqualTo("[{\"name\":\"build\"},{\"name\":\"deploy\"}]");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("Deploy Pipeline");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void findDefinitionById_whenFound_returnsMappedDomain() {
        WorkflowDefinitionEntity entity = WorkflowDefinitionEntity.builder()
                .id(TEST_UUID).name("Build").triggerEvent("commit")
                .stepsJson("[{\"name\":\"compile\"}]").active(true).createdAt(NOW).build();

        when(definitionJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<WorkflowDefinition> result = adapter.findDefinitionById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getName()).isEqualTo("Build");
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void findDefinitionById_whenNotFound_returnsEmpty() {
        when(definitionJpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<WorkflowDefinition> result = adapter.findDefinitionById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllDefinitions_mapsAllEntities() {
        WorkflowDefinitionEntity e1 = WorkflowDefinitionEntity.builder()
                .id(TEST_UUID).name("Build").triggerEvent("commit")
                .stepsJson("[{}]").active(true).createdAt(NOW).build();
        WorkflowDefinitionEntity e2 = WorkflowDefinitionEntity.builder()
                .id(TEST_UUID_2).name("Deploy").triggerEvent("merge")
                .stepsJson("[{}]").active(false).createdAt(NOW).build();

        when(definitionJpaRepository.findAll()).thenReturn(List.of(e1, e2));

        List<WorkflowDefinition> result = adapter.findAllDefinitions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Build");
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(1).getName()).isEqualTo("Deploy");
        assertThat(result.get(1).isActive()).isFalse();
    }

    @Test
    void saveDefinition_withNullId_doesNotSetIdOnEntity() {
        WorkflowDefinition domain = WorkflowDefinition.builder()
                .name("New Workflow")
                .triggerEvent("event")
                .stepsJson("[{}]")
                .active(true)
                .createdAt(NOW)
                .build();

        WorkflowDefinitionEntity savedEntity = WorkflowDefinitionEntity.builder()
                .id(TEST_UUID).name("New Workflow").triggerEvent("event")
                .stepsJson("[{}]").active(true).createdAt(NOW).build();

        when(definitionJpaRepository.save(any(WorkflowDefinitionEntity.class))).thenReturn(savedEntity);

        adapter.saveDefinition(domain);

        ArgumentCaptor<WorkflowDefinitionEntity> captor = ArgumentCaptor.forClass(WorkflowDefinitionEntity.class);
        verify(definitionJpaRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isNull();
    }

    // --- WorkflowRun Tests ---

    @Test
    void saveRun_mapsDomainToEntityAndBack() {
        WorkflowRun domain = WorkflowRun.builder()
                .id(TEST_UUID.toString())
                .workflowId("wf-1")
                .status("completed")
                .payloadJson("{\"branch\":\"main\"}")
                .stepResultsJson("[{\"stepIndex\":0,\"status\":\"completed\"}]")
                .startedAt(NOW)
                .completedAt(COMPLETED)
                .build();

        WorkflowRunEntity savedEntity = WorkflowRunEntity.builder()
                .id(TEST_UUID)
                .workflowId("wf-1")
                .status("completed")
                .payloadJson("{\"branch\":\"main\"}")
                .stepResultsJson("[{\"stepIndex\":0,\"status\":\"completed\"}]")
                .startedAt(NOW)
                .completedAt(COMPLETED)
                .build();

        when(runJpaRepository.save(any(WorkflowRunEntity.class))).thenReturn(savedEntity);

        WorkflowRun result = adapter.saveRun(domain);

        ArgumentCaptor<WorkflowRunEntity> captor = ArgumentCaptor.forClass(WorkflowRunEntity.class);
        verify(runJpaRepository).save(captor.capture());

        WorkflowRunEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getWorkflowId()).isEqualTo("wf-1");
        assertThat(captured.getStatus()).isEqualTo("completed");
        assertThat(captured.getStartedAt()).isEqualTo(NOW);
        assertThat(captured.getCompletedAt()).isEqualTo(COMPLETED);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getWorkflowId()).isEqualTo("wf-1");
        assertThat(result.getStatus()).isEqualTo("completed");
    }

    @Test
    void findRunById_whenFound_returnsMappedDomain() {
        WorkflowRunEntity entity = WorkflowRunEntity.builder()
                .id(TEST_UUID).workflowId("wf-1").status("completed")
                .payloadJson("{}").stepResultsJson("[{}]")
                .startedAt(NOW).completedAt(COMPLETED).build();

        when(runJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<WorkflowRun> result = adapter.findRunById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getWorkflowId()).isEqualTo("wf-1");
        assertThat(result.get().getStatus()).isEqualTo("completed");
    }

    @Test
    void findRunById_whenNotFound_returnsEmpty() {
        when(runJpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<WorkflowRun> result = adapter.findRunById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }
}
