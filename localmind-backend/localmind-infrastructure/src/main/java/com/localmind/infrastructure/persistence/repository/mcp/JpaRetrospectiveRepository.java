package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.RetrospectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for RetrospectiveEntity.
 */
public interface JpaRetrospectiveRepository extends JpaRepository<RetrospectiveEntity, UUID> {
}
