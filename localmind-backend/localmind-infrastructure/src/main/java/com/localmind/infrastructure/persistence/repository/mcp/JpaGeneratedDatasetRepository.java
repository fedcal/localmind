package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.GeneratedDatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for GeneratedDatasetEntity.
 */
public interface JpaGeneratedDatasetRepository extends JpaRepository<GeneratedDatasetEntity, UUID> {
}
