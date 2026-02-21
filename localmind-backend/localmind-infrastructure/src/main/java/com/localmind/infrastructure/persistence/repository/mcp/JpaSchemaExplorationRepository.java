package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.SchemaExplorationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for SchemaExplorationEntity.
 */
public interface JpaSchemaExplorationRepository extends JpaRepository<SchemaExplorationEntity, UUID> {
}
