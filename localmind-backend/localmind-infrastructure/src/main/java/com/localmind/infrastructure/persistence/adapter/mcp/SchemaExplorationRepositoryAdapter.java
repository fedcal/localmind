package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.SchemaExploration;
import com.localmind.domain.mcp.port.out.SchemaExplorationRepository;
import com.localmind.infrastructure.persistence.entity.mcp.SchemaExplorationEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaSchemaExplorationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter bridging the domain SchemaExplorationRepository port to JPA persistence.
 */
@Component
public class SchemaExplorationRepositoryAdapter implements SchemaExplorationRepository {

    private final JpaSchemaExplorationRepository jpaRepository;

    public SchemaExplorationRepositoryAdapter(JpaSchemaExplorationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SchemaExploration save(SchemaExploration exploration) {
        SchemaExplorationEntity entity = toEntity(exploration);
        SchemaExplorationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private SchemaExplorationEntity toEntity(SchemaExploration domain) {
        SchemaExplorationEntity entity = SchemaExplorationEntity.builder()
                .jdbcUrl(domain.getJdbcUrl())
                .tableCount(domain.getTableCount())
                .result(domain.getResultJson())
                .exploredAt(domain.getExploredAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private SchemaExploration toDomain(SchemaExplorationEntity entity) {
        return SchemaExploration.builder()
                .id(entity.getId().toString())
                .jdbcUrl(entity.getJdbcUrl())
                .tableCount(entity.getTableCount())
                .resultJson(entity.getResult())
                .exploredAt(entity.getExploredAt())
                .build();
    }
}
