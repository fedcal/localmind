package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.DecisionLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for DecisionLinkEntity.
 */
public interface JpaDecisionLinkRepository extends JpaRepository<DecisionLinkEntity, UUID> {

    List<DecisionLinkEntity> findByDecisionId(String decisionId);
}
