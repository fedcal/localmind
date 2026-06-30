package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.InsightResult;
import com.localmind.domain.mcp.port.out.InsightRepository;
import com.localmind.infrastructure.persistence.entity.mcp.InsightResultEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaInsightResultRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InsightRepositoryAdapter implements InsightRepository {

    private final JpaInsightResultRepository jpaRepository;

    public InsightRepositoryAdapter(JpaInsightResultRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InsightResult save(InsightResult insight) {
        InsightResultEntity entity = toEntity(insight);
        InsightResultEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<InsightResult> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private InsightResultEntity toEntity(InsightResult domain) {
        InsightResultEntity entity = InsightResultEntity.builder()
                .question(domain.getQuestion())
                .resultJson(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private InsightResult toDomain(InsightResultEntity entity) {
        return InsightResult.builder()
                .id(entity.getId().toString())
                .question(entity.getQuestion())
                .resultJson(entity.getResultJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
