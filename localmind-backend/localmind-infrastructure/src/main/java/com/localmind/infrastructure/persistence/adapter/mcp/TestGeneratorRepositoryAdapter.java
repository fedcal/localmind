package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.GeneratedTest;
import com.localmind.domain.mcp.port.out.TestGeneratorRepository;
import com.localmind.infrastructure.persistence.entity.mcp.GeneratedTestEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaGeneratedTestRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestGeneratorRepositoryAdapter implements TestGeneratorRepository {

    private final JpaGeneratedTestRepository jpaRepository;

    public TestGeneratorRepositoryAdapter(JpaGeneratedTestRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GeneratedTest save(GeneratedTest generatedTest) {
        GeneratedTestEntity entity = toEntity(generatedTest);
        GeneratedTestEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private GeneratedTestEntity toEntity(GeneratedTest domain) {
        GeneratedTestEntity entity = GeneratedTestEntity.builder()
                .sourceFilePath(domain.getSourceFilePath())
                .language(domain.getLanguage())
                .framework(domain.getFramework())
                .functionsFound(domain.getFunctionsFound())
                .testCode(domain.getTestCode())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private GeneratedTest toDomain(GeneratedTestEntity entity) {
        return GeneratedTest.builder()
                .id(entity.getId().toString())
                .sourceFilePath(entity.getSourceFilePath())
                .language(entity.getLanguage())
                .framework(entity.getFramework())
                .functionsFound(entity.getFunctionsFound())
                .testCode(entity.getTestCode())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
