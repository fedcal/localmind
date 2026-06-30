package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.ScaffoldedProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaScaffoldedProjectRepository extends JpaRepository<ScaffoldedProjectEntity, UUID> {
}
