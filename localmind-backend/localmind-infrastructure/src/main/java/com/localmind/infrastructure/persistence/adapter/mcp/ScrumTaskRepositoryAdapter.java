package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ScrumTask;
import com.localmind.domain.mcp.port.out.ScrumTaskRepository;
import com.localmind.infrastructure.persistence.entity.mcp.ScrumTaskEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaScrumTaskRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain ScrumTaskRepository port to JPA persistence.
 */
@Component
public class ScrumTaskRepositoryAdapter implements ScrumTaskRepository {

    private final JpaScrumTaskRepository jpaScrumTaskRepository;

    public ScrumTaskRepositoryAdapter(JpaScrumTaskRepository jpaScrumTaskRepository) {
        this.jpaScrumTaskRepository = jpaScrumTaskRepository;
    }

    @Override
    public ScrumTask save(ScrumTask task) {
        ScrumTaskEntity entity = toEntity(task);
        ScrumTaskEntity saved = jpaScrumTaskRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ScrumTask> findByStoryId(String storyId) {
        return jpaScrumTaskRepository.findByStoryId(storyId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ScrumTask> findById(String id) {
        return jpaScrumTaskRepository.findById(UUID.fromString(id))
                .map(this::toDomain);
    }

    private ScrumTaskEntity toEntity(ScrumTask domain) {
        ScrumTaskEntity entity = ScrumTaskEntity.builder()
                .title(domain.getTitle())
                .description(domain.getDescription())
                .storyId(domain.getStoryId())
                .assignee(domain.getAssignee())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private ScrumTask toDomain(ScrumTaskEntity entity) {
        return ScrumTask.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .storyId(entity.getStoryId())
                .assignee(entity.getAssignee())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
