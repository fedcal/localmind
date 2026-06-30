package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.WorkflowRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaWorkflowRunRepository extends JpaRepository<WorkflowRunEntity, UUID> {
}
