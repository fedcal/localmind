package com.localmind.infrastructure.marketplace.persistence.repository;

import com.localmind.infrastructure.marketplace.persistence.entity.MarketplaceReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarketplaceReviewJpaRepository extends JpaRepository<MarketplaceReviewEntity, UUID> {

    List<MarketplaceReviewEntity> findByAgentId(String agentId);
}
