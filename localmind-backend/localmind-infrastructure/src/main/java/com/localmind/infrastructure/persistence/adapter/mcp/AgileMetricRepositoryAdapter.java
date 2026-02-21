package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.AgileMetric;
import com.localmind.domain.mcp.port.out.AgileMetricRepository;
import com.localmind.infrastructure.persistence.entity.mcp.AgileMetricEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaAgileMetricRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain AgileMetricRepository port to JPA persistence.
 */
@Component
public class AgileMetricRepositoryAdapter implements AgileMetricRepository {

    private final JpaAgileMetricRepository jpaRepository;

    public AgileMetricRepositoryAdapter(JpaAgileMetricRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AgileMetric save(AgileMetric agileMetric) {
        AgileMetricEntity entity = toEntity(agileMetric);
        AgileMetricEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AgileMetric> findByMetricType(String metricType) {
        return jpaRepository.findByMetricTypeOrderByCreatedAtDesc(metricType)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AgileMetricEntity toEntity(AgileMetric domain) {
        AgileMetricEntity entity = AgileMetricEntity.builder()
                .metricType(domain.getMetricType())
                .sprintRef(domain.getSprintRef())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private AgileMetric toDomain(AgileMetricEntity entity) {
        return AgileMetric.builder()
                .id(entity.getId().toString())
                .metricType(entity.getMetricType())
                .sprintRef(entity.getSprintRef())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
