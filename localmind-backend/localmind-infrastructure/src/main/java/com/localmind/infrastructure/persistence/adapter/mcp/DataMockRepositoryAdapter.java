package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.GeneratedDataset;
import com.localmind.domain.mcp.port.out.DataMockRepository;
import com.localmind.infrastructure.persistence.entity.mcp.GeneratedDatasetEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaGeneratedDatasetRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter bridging the domain DataMockRepository port to JPA persistence.
 */
@Component
public class DataMockRepositoryAdapter implements DataMockRepository {

    private final JpaGeneratedDatasetRepository jpaRepository;

    public DataMockRepositoryAdapter(JpaGeneratedDatasetRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GeneratedDataset save(GeneratedDataset dataset) {
        GeneratedDatasetEntity entity = toEntity(dataset);
        GeneratedDatasetEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private GeneratedDatasetEntity toEntity(GeneratedDataset domain) {
        GeneratedDatasetEntity entity = GeneratedDatasetEntity.builder()
                .name(domain.getName())
                .format(domain.getFormat())
                .rowCount(domain.getRowCount())
                .schemaJson(domain.getSchemaJson())
                .sampleData(domain.getSampleDataJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private GeneratedDataset toDomain(GeneratedDatasetEntity entity) {
        return GeneratedDataset.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .format(entity.getFormat())
                .rowCount(entity.getRowCount())
                .schemaJson(entity.getSchemaJson())
                .sampleDataJson(entity.getSampleData())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
