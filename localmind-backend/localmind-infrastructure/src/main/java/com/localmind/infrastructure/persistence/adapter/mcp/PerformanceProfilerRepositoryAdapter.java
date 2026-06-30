package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.BenchmarkResult;
import com.localmind.domain.mcp.port.out.PerformanceProfilerRepository;
import com.localmind.infrastructure.persistence.entity.mcp.BenchmarkResultEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaBenchmarkResultRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter bridging the domain PerformanceProfilerRepository port to JPA persistence.
 */
@Component
public class PerformanceProfilerRepositoryAdapter implements PerformanceProfilerRepository {

    private final JpaBenchmarkResultRepository jpaRepository;

    public PerformanceProfilerRepositoryAdapter(JpaBenchmarkResultRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BenchmarkResult save(BenchmarkResult result) {
        BenchmarkResultEntity entity = toEntity(result);
        BenchmarkResultEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private BenchmarkResultEntity toEntity(BenchmarkResult domain) {
        BenchmarkResultEntity entity = BenchmarkResultEntity.builder()
                .name(domain.getName())
                .language(domain.getLanguage())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private BenchmarkResult toDomain(BenchmarkResultEntity entity) {
        return BenchmarkResult.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .language(entity.getLanguage())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
