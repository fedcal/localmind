package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.FeatureCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaFeatureCostRepository extends JpaRepository<FeatureCostEntity, UUID> {

    List<FeatureCostEntity> findByProjectNameOrderByCreatedAtDesc(String projectName);
}
