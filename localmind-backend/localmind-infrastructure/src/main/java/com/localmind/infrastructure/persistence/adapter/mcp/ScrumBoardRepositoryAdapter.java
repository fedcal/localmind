package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.Sprint;
import com.localmind.domain.mcp.port.out.SprintRepository;
import com.localmind.infrastructure.persistence.entity.mcp.SprintEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaSprintRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain SprintRepository port to JPA persistence.
 */
@Component
public class ScrumBoardRepositoryAdapter implements SprintRepository {

    private final JpaSprintRepository jpaSprintRepository;

    public ScrumBoardRepositoryAdapter(JpaSprintRepository jpaSprintRepository) {
        this.jpaSprintRepository = jpaSprintRepository;
    }

    @Override
    public Sprint save(Sprint sprint) {
        SprintEntity entity = toSprintEntity(sprint);
        SprintEntity saved = jpaSprintRepository.save(entity);
        return toSprintDomain(saved);
    }

    @Override
    public Optional<Sprint> findById(String id) {
        return jpaSprintRepository.findById(UUID.fromString(id))
                .map(this::toSprintDomain);
    }

    @Override
    public List<Sprint> findAll() {
        return jpaSprintRepository.findAll()
                .stream()
                .map(this::toSprintDomain)
                .collect(Collectors.toList());
    }

    private SprintEntity toSprintEntity(Sprint domain) {
        SprintEntity entity = SprintEntity.builder()
                .name(domain.getName())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .goals(domain.getGoalsJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private Sprint toSprintDomain(SprintEntity entity) {
        return Sprint.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .goalsJson(entity.getGoals())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
