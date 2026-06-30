package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.WorkflowDefinition;
import com.localmind.domain.mcp.model.WorkflowRun;
import com.localmind.domain.mcp.port.out.WorkflowRepository;
import com.localmind.infrastructure.persistence.entity.mcp.WorkflowDefinitionEntity;
import com.localmind.infrastructure.persistence.entity.mcp.WorkflowRunEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaWorkflowDefinitionRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaWorkflowRunRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain WorkflowRepository port to JPA persistence.
 * Handles mapping between WorkflowDefinition/WorkflowRun domain models and their entities.
 */
@Component
public class WorkflowRepositoryAdapter implements WorkflowRepository {

    private final JpaWorkflowDefinitionRepository definitionJpaRepository;
    private final JpaWorkflowRunRepository runJpaRepository;

    public WorkflowRepositoryAdapter(JpaWorkflowDefinitionRepository definitionJpaRepository,
                                      JpaWorkflowRunRepository runJpaRepository) {
        this.definitionJpaRepository = definitionJpaRepository;
        this.runJpaRepository = runJpaRepository;
    }

    // --- WorkflowDefinition ---

    @Override
    public WorkflowDefinition saveDefinition(WorkflowDefinition definition) {
        WorkflowDefinitionEntity entity = toDefinitionEntity(definition);
        WorkflowDefinitionEntity saved = definitionJpaRepository.save(entity);
        return toDefinitionDomain(saved);
    }

    @Override
    public Optional<WorkflowDefinition> findDefinitionById(String id) {
        return definitionJpaRepository.findById(UUID.fromString(id))
                .map(this::toDefinitionDomain);
    }

    @Override
    public List<WorkflowDefinition> findAllDefinitions() {
        return definitionJpaRepository.findAll().stream()
                .map(this::toDefinitionDomain)
                .collect(Collectors.toList());
    }

    // --- WorkflowRun ---

    @Override
    public WorkflowRun saveRun(WorkflowRun run) {
        WorkflowRunEntity entity = toRunEntity(run);
        WorkflowRunEntity saved = runJpaRepository.save(entity);
        return toRunDomain(saved);
    }

    @Override
    public Optional<WorkflowRun> findRunById(String id) {
        return runJpaRepository.findById(UUID.fromString(id))
                .map(this::toRunDomain);
    }

    // --- WorkflowDefinition Mapping ---

    private WorkflowDefinitionEntity toDefinitionEntity(WorkflowDefinition domain) {
        WorkflowDefinitionEntity entity = WorkflowDefinitionEntity.builder()
                .name(domain.getName())
                .description(domain.getDescription())
                .triggerEvent(domain.getTriggerEvent())
                .triggerConditionsJson(domain.getTriggerConditionsJson())
                .stepsJson(domain.getStepsJson())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private WorkflowDefinition toDefinitionDomain(WorkflowDefinitionEntity entity) {
        return WorkflowDefinition.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .triggerEvent(entity.getTriggerEvent())
                .triggerConditionsJson(entity.getTriggerConditionsJson())
                .stepsJson(entity.getStepsJson())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- WorkflowRun Mapping ---

    private WorkflowRunEntity toRunEntity(WorkflowRun domain) {
        WorkflowRunEntity entity = WorkflowRunEntity.builder()
                .workflowId(domain.getWorkflowId())
                .status(domain.getStatus())
                .payloadJson(domain.getPayloadJson())
                .stepResultsJson(domain.getStepResultsJson())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private WorkflowRun toRunDomain(WorkflowRunEntity entity) {
        return WorkflowRun.builder()
                .id(entity.getId().toString())
                .workflowId(entity.getWorkflowId())
                .status(entity.getStatus())
                .payloadJson(entity.getPayloadJson())
                .stepResultsJson(entity.getStepResultsJson())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
