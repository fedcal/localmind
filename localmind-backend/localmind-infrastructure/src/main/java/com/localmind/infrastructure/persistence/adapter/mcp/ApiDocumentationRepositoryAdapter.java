package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ApiDocumentation;
import com.localmind.domain.mcp.port.out.ApiDocumentationRepository;
import com.localmind.infrastructure.persistence.entity.mcp.ApiDocumentationEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaApiDocumentationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApiDocumentationRepositoryAdapter implements ApiDocumentationRepository {

    private final JpaApiDocumentationRepository jpaRepository;

    public ApiDocumentationRepositoryAdapter(JpaApiDocumentationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApiDocumentation save(ApiDocumentation documentation) {
        ApiDocumentationEntity entity = toEntity(documentation);
        ApiDocumentationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private ApiDocumentationEntity toEntity(ApiDocumentation domain) {
        ApiDocumentationEntity entity = ApiDocumentationEntity.builder()
                .documentationType(domain.getDocumentationType())
                .filePath(domain.getFilePath())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private ApiDocumentation toDomain(ApiDocumentationEntity entity) {
        return ApiDocumentation.builder()
                .id(entity.getId().toString())
                .documentationType(entity.getDocumentationType())
                .filePath(entity.getFilePath())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
