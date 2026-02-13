package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.ApiDocumentationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaApiDocumentationRepository extends JpaRepository<ApiDocumentationEntity, UUID> {
}
