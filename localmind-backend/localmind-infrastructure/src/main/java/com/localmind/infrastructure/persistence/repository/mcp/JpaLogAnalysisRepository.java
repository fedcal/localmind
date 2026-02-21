package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.LogAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for LogAnalysisEntity.
 */
public interface JpaLogAnalysisRepository extends JpaRepository<LogAnalysisEntity, UUID> {
}
