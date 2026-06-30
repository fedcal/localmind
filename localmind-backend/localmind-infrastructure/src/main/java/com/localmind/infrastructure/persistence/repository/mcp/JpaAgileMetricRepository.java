package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.AgileMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaAgileMetricRepository extends JpaRepository<AgileMetricEntity, UUID> {

    List<AgileMetricEntity> findByMetricTypeOrderByCreatedAtDesc(String metricType);
}
