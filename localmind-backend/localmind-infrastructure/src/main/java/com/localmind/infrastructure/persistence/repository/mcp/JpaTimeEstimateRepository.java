package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.TimeEstimateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for TimeEstimateEntity.
 */
public interface JpaTimeEstimateRepository extends JpaRepository<TimeEstimateEntity, UUID> {

    /**
     * Finds the estimate for a given task.
     */
    Optional<TimeEstimateEntity> findByTaskId(String taskId);
}
