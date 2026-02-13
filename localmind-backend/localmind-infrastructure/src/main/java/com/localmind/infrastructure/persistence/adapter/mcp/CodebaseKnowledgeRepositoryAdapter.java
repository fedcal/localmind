package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CodebaseKnowledge;
import com.localmind.domain.mcp.port.out.CodebaseKnowledgeRepository;
import com.localmind.infrastructure.persistence.entity.mcp.CodebaseKnowledgeEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaCodebaseKnowledgeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CodebaseKnowledgeRepositoryAdapter implements CodebaseKnowledgeRepository {

    private final JpaCodebaseKnowledgeRepository jpaRepository;

    public CodebaseKnowledgeRepositoryAdapter(JpaCodebaseKnowledgeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CodebaseKnowledge save(CodebaseKnowledge knowledge) {
        CodebaseKnowledgeEntity entity = toEntity(knowledge);
        CodebaseKnowledgeEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<CodebaseKnowledge> findByTargetPath(String targetPath) {
        return jpaRepository.findByTargetPathOrderByCreatedAtDesc(targetPath)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CodebaseKnowledgeEntity toEntity(CodebaseKnowledge domain) {
        CodebaseKnowledgeEntity entity = CodebaseKnowledgeEntity.builder()
                .analysisType(domain.getAnalysisType())
                .targetPath(domain.getTargetPath())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private CodebaseKnowledge toDomain(CodebaseKnowledgeEntity entity) {
        return CodebaseKnowledge.builder()
                .id(entity.getId().toString())
                .analysisType(entity.getAnalysisType())
                .targetPath(entity.getTargetPath())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
