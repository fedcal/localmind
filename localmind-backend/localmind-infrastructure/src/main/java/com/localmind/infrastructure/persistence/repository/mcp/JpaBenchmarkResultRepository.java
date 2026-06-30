package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.BenchmarkResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for BenchmarkResultEntity.
 */
public interface JpaBenchmarkResultRepository extends JpaRepository<BenchmarkResultEntity, UUID> {
}
