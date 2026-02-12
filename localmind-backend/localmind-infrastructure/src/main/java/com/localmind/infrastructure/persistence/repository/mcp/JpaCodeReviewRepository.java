package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.CodeReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for CodeReviewEntity.
 */
public interface JpaCodeReviewRepository extends JpaRepository<CodeReviewEntity, UUID> {
}
