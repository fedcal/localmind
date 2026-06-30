package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.HttpRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for HttpRequestEntity.
 */
public interface JpaHttpRequestRepository extends JpaRepository<HttpRequestEntity, UUID> {
}
