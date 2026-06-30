package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ScaffoldedProject;
import com.localmind.domain.mcp.port.out.ScaffoldingRepository;
import com.localmind.infrastructure.persistence.entity.mcp.ScaffoldedProjectEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaScaffoldedProjectRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScaffoldingRepositoryAdapter implements ScaffoldingRepository {

    private final JpaScaffoldedProjectRepository jpaRepository;

    public ScaffoldingRepositoryAdapter(JpaScaffoldedProjectRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ScaffoldedProject save(ScaffoldedProject project) {
        ScaffoldedProjectEntity entity = toEntity(project);
        ScaffoldedProjectEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private ScaffoldedProjectEntity toEntity(ScaffoldedProject domain) {
        ScaffoldedProjectEntity entity = ScaffoldedProjectEntity.builder()
                .templateName(domain.getTemplateName())
                .projectName(domain.getProjectName())
                .outputPath(domain.getOutputPath())
                .filesGenerated(domain.getFilesGenerated())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private ScaffoldedProject toDomain(ScaffoldedProjectEntity entity) {
        return ScaffoldedProject.builder()
                .id(entity.getId().toString())
                .templateName(entity.getTemplateName())
                .projectName(entity.getProjectName())
                .outputPath(entity.getOutputPath())
                .filesGenerated(entity.getFilesGenerated())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
