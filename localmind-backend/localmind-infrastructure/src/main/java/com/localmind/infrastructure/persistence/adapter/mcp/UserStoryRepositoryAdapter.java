package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.UserStory;
import com.localmind.domain.mcp.port.out.UserStoryRepository;
import com.localmind.infrastructure.persistence.entity.mcp.UserStoryEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaUserStoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain UserStoryRepository port to JPA persistence.
 */
@Component
public class UserStoryRepositoryAdapter implements UserStoryRepository {

    private final JpaUserStoryRepository jpaUserStoryRepository;

    public UserStoryRepositoryAdapter(JpaUserStoryRepository jpaUserStoryRepository) {
        this.jpaUserStoryRepository = jpaUserStoryRepository;
    }

    @Override
    public UserStory save(UserStory userStory) {
        UserStoryEntity entity = toEntity(userStory);
        UserStoryEntity saved = jpaUserStoryRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<UserStory> findBySprintId(String sprintId) {
        return jpaUserStoryRepository.findBySprintId(sprintId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserStory> findUnassigned() {
        return jpaUserStoryRepository.findBySprintIdIsNull()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private UserStoryEntity toEntity(UserStory domain) {
        UserStoryEntity entity = UserStoryEntity.builder()
                .title(domain.getTitle())
                .description(domain.getDescription())
                .acceptanceCriteria(domain.getAcceptanceCriteriaJson())
                .storyPoints(domain.getStoryPoints())
                .priority(domain.getPriority())
                .sprintId(domain.getSprintId())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private UserStory toDomain(UserStoryEntity entity) {
        return UserStory.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .acceptanceCriteriaJson(entity.getAcceptanceCriteria())
                .storyPoints(entity.getStoryPoints())
                .priority(entity.getPriority())
                .sprintId(entity.getSprintId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
