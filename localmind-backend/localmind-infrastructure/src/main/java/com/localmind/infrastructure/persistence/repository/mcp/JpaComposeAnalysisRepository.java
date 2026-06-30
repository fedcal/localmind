package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.ComposeAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaComposeAnalysisRepository extends JpaRepository<ComposeAnalysisEntity, UUID> {
}
