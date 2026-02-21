package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ComposeAnalysis;
import com.localmind.domain.mcp.port.out.DockerAnalysisRepository;
import com.localmind.infrastructure.persistence.entity.mcp.ComposeAnalysisEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaComposeAnalysisRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DockerAnalysisRepositoryAdapter implements DockerAnalysisRepository {

    private final JpaComposeAnalysisRepository jpaRepository;

    public DockerAnalysisRepositoryAdapter(JpaComposeAnalysisRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ComposeAnalysis save(ComposeAnalysis analysis) {
        ComposeAnalysisEntity entity = toEntity(analysis);
        ComposeAnalysisEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private ComposeAnalysisEntity toEntity(ComposeAnalysis domain) {
        ComposeAnalysisEntity entity = ComposeAnalysisEntity.builder()
                .contentType(domain.getContentType())
                .serviceCount(domain.getServiceCount())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private ComposeAnalysis toDomain(ComposeAnalysisEntity entity) {
        return ComposeAnalysis.builder()
                .id(entity.getId().toString())
                .contentType(entity.getContentType())
                .serviceCount(entity.getServiceCount())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
