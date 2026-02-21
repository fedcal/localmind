package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CodeReviewResult;
import com.localmind.domain.mcp.port.out.CodeReviewRepository;
import com.localmind.infrastructure.persistence.entity.mcp.CodeReviewEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaCodeReviewRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter bridging the domain CodeReviewRepository port to JPA persistence.
 */
@Component
public class CodeReviewRepositoryAdapter implements CodeReviewRepository {

    private final JpaCodeReviewRepository jpaRepository;

    public CodeReviewRepositoryAdapter(JpaCodeReviewRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CodeReviewResult save(CodeReviewResult result) {
        CodeReviewEntity entity = toEntity(result);
        CodeReviewEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private CodeReviewEntity toEntity(CodeReviewResult domain) {
        CodeReviewEntity entity = CodeReviewEntity.builder()
                .reviewType(domain.getReviewType())
                .issuesFound(domain.getIssuesFound())
                .suggestions(domain.getSuggestionsJson())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private CodeReviewResult toDomain(CodeReviewEntity entity) {
        return CodeReviewResult.builder()
                .id(entity.getId().toString())
                .reviewType(entity.getReviewType())
                .issuesFound(entity.getIssuesFound())
                .suggestionsJson(entity.getSuggestions())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
