package com.localmind.infrastructure.marketplace.persistence.repository;

import com.localmind.infrastructure.marketplace.persistence.entity.MarketplaceAgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MarketplaceAgentJpaRepository extends JpaRepository<MarketplaceAgentEntity, UUID> {

    List<MarketplaceAgentEntity> findByCategory(String category);

    List<MarketplaceAgentEntity> findByNameContainingIgnoreCase(String query);

    @Modifying
    @Transactional
    @Query("UPDATE MarketplaceAgentEntity a SET a.downloadCount = a.downloadCount + 1 WHERE a.id = :id")
    void incrementDownloadCount(@Param("id") UUID id);
}
