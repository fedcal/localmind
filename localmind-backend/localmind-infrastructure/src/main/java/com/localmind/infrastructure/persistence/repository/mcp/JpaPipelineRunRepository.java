package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.PipelineRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for PipelineRunEntity.
 */
public interface JpaPipelineRunRepository extends JpaRepository<PipelineRunEntity, UUID> {

    List<PipelineRunEntity> findByOwnerAndRepoOrderByCreatedAtDesc(String owner, String repo);

    Optional<PipelineRunEntity> findByRunId(String runId);
}
