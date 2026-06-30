package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.Incident;
import com.localmind.domain.mcp.port.out.IncidentRepository;
import com.localmind.infrastructure.persistence.entity.mcp.IncidentEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaIncidentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain IncidentRepository port to JPA persistence.
 * Handles mapping between Incident domain model and IncidentEntity.
 */
@Component
public class IncidentRepositoryAdapter implements IncidentRepository {

    private final JpaIncidentRepository jpaRepository;

    public IncidentRepositoryAdapter(JpaIncidentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = toEntity(incident);
        IncidentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Incident> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id))
                .map(this::toDomain);
    }

    @Override
    public List<Incident> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private IncidentEntity toEntity(Incident domain) {
        IncidentEntity entity = IncidentEntity.builder()
                .title(domain.getTitle())
                .severity(domain.getSeverity())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .affectedSystemsJson(domain.getAffectedSystemsJson())
                .timelineJson(domain.getTimelineJson())
                .resolution(domain.getResolution())
                .rootCause(domain.getRootCause())
                .createdAt(domain.getCreatedAt())
                .resolvedAt(domain.getResolvedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private Incident toDomain(IncidentEntity entity) {
        return Incident.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .affectedSystemsJson(entity.getAffectedSystemsJson())
                .timelineJson(entity.getTimelineJson())
                .resolution(entity.getResolution())
                .rootCause(entity.getRootCause())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
