package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.DecisionRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for DecisionRecordEntity.
 */
public interface JpaDecisionRecordRepository extends JpaRepository<DecisionRecordEntity, UUID> {
}
