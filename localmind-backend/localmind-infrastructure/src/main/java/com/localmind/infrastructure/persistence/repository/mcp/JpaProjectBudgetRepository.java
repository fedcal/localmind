package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.ProjectBudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaProjectBudgetRepository extends JpaRepository<ProjectBudgetEntity, UUID> {

    Optional<ProjectBudgetEntity> findByProjectName(String projectName);
}
