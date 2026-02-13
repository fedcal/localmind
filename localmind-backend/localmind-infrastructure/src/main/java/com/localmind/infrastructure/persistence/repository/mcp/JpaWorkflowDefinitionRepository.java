package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.WorkflowDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaWorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, UUID> {
}
