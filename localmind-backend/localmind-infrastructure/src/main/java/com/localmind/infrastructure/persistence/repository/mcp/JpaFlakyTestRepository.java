package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.FlakyTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for FlakyTestEntity.
 */
public interface JpaFlakyTestRepository extends JpaRepository<FlakyTestEntity, UUID> {
}
