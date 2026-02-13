package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.SprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for SprintEntity.
 */
public interface JpaSprintRepository extends JpaRepository<SprintEntity, UUID> {
}
