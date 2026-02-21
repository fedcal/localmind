package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.LogAnalysis;
import com.localmind.domain.mcp.port.out.LogAnalysisRepository;
import com.localmind.infrastructure.persistence.entity.mcp.LogAnalysisEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaLogAnalysisRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter bridging the domain LogAnalysisRepository port to JPA persistence.
 */
@Component
public class LogAnalysisRepositoryAdapter implements LogAnalysisRepository {

    private final JpaLogAnalysisRepository jpaRepository;

    public LogAnalysisRepositoryAdapter(JpaLogAnalysisRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LogAnalysis save(LogAnalysis logAnalysis) {
        LogAnalysisEntity entity = toEntity(logAnalysis);
        LogAnalysisEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private LogAnalysisEntity toEntity(LogAnalysis domain) {
        LogAnalysisEntity entity = LogAnalysisEntity.builder()
                .totalLines(domain.getTotalLines())
                .levels(domain.getLevelsJson())
                .topErrors(domain.getTopErrorsJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private LogAnalysis toDomain(LogAnalysisEntity entity) {
        return LogAnalysis.builder()
                .id(entity.getId().toString())
                .totalLines(entity.getTotalLines())
                .levelsJson(entity.getLevels())
                .topErrorsJson(entity.getTopErrors())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
