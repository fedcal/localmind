package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.GeneratedTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaGeneratedTestRepository extends JpaRepository<GeneratedTestEntity, UUID> {
}
