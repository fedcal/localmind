package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.StandupNote;
import com.localmind.domain.mcp.port.out.StandupRepository;
import com.localmind.infrastructure.persistence.entity.mcp.StandupNoteEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaStandupNoteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain StandupRepository port to JPA persistence.
 */
@Component
public class StandupRepositoryAdapter implements StandupRepository {

    private final JpaStandupNoteRepository jpaRepository;

    public StandupRepositoryAdapter(JpaStandupNoteRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StandupNote save(StandupNote standupNote) {
        StandupNoteEntity entity = toEntity(standupNote);
        StandupNoteEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<StandupNote> findAll() {
        return jpaRepository.findAllByOrderByStandupDateDesc()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private StandupNoteEntity toEntity(StandupNote domain) {
        StandupNoteEntity entity = StandupNoteEntity.builder()
                .yesterday(domain.getYesterday())
                .today(domain.getToday())
                .blockers(domain.getBlockers())
                .standupDate(domain.getStandupDate())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private StandupNote toDomain(StandupNoteEntity entity) {
        return StandupNote.builder()
                .id(entity.getId().toString())
                .yesterday(entity.getYesterday())
                .today(entity.getToday())
                .blockers(entity.getBlockers())
                .standupDate(entity.getStandupDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
