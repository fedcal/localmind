package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ActionItem;
import com.localmind.domain.mcp.model.RetroItem;
import com.localmind.domain.mcp.model.Retrospective;
import com.localmind.domain.mcp.port.out.RetrospectiveRepository;
import com.localmind.infrastructure.persistence.entity.mcp.ActionItemEntity;
import com.localmind.infrastructure.persistence.entity.mcp.RetroItemEntity;
import com.localmind.infrastructure.persistence.entity.mcp.RetrospectiveEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaActionItemRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaRetroItemRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaRetrospectiveRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain RetrospectiveRepository port to JPA persistence.
 * Handles mapping between domain models and JPA entities for retrospectives,
 * retro items, and action items.
 */
@Component
public class RetrospectiveRepositoryAdapter implements RetrospectiveRepository {

    private final JpaRetrospectiveRepository retroJpaRepository;
    private final JpaRetroItemRepository itemJpaRepository;
    private final JpaActionItemRepository actionItemJpaRepository;

    public RetrospectiveRepositoryAdapter(JpaRetrospectiveRepository retroJpaRepository,
                                           JpaRetroItemRepository itemJpaRepository,
                                           JpaActionItemRepository actionItemJpaRepository) {
        this.retroJpaRepository = retroJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.actionItemJpaRepository = actionItemJpaRepository;
    }

    // --- Retrospective ---

    @Override
    public Retrospective saveRetro(Retrospective retrospective) {
        RetrospectiveEntity entity = toRetroEntity(retrospective);
        RetrospectiveEntity saved = retroJpaRepository.save(entity);
        return toRetroDomain(saved);
    }

    @Override
    public Optional<Retrospective> findRetroById(String id) {
        return retroJpaRepository.findById(UUID.fromString(id))
                .map(this::toRetroDomain);
    }

    @Override
    public List<Retrospective> findAllRetros() {
        return retroJpaRepository.findAll().stream()
                .map(this::toRetroDomain)
                .collect(Collectors.toList());
    }

    // --- RetroItem ---

    @Override
    public RetroItem saveItem(RetroItem item) {
        RetroItemEntity entity = toItemEntity(item);
        RetroItemEntity saved = itemJpaRepository.save(entity);
        return toItemDomain(saved);
    }

    @Override
    public List<RetroItem> findItemsByRetroId(String retroId) {
        return itemJpaRepository.findByRetroId(retroId).stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RetroItem> findItemById(String id) {
        return itemJpaRepository.findById(UUID.fromString(id))
                .map(this::toItemDomain);
    }

    @Override
    public List<RetroItem> findAllItems() {
        return itemJpaRepository.findAll().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());
    }

    // --- ActionItem ---

    @Override
    public ActionItem saveActionItem(ActionItem actionItem) {
        ActionItemEntity entity = toActionItemEntity(actionItem);
        ActionItemEntity saved = actionItemJpaRepository.save(entity);
        return toActionItemDomain(saved);
    }

    @Override
    public List<ActionItem> findActionItemsByRetroId(String retroId) {
        return actionItemJpaRepository.findByRetroId(retroId).stream()
                .map(this::toActionItemDomain)
                .collect(Collectors.toList());
    }

    // --- Retrospective Mapping ---

    private RetrospectiveEntity toRetroEntity(Retrospective domain) {
        RetrospectiveEntity entity = RetrospectiveEntity.builder()
                .sprintId(domain.getSprintId())
                .format(domain.getFormat())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private Retrospective toRetroDomain(RetrospectiveEntity entity) {
        return Retrospective.builder()
                .id(entity.getId().toString())
                .sprintId(entity.getSprintId())
                .format(entity.getFormat())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- RetroItem Mapping ---

    private RetroItemEntity toItemEntity(RetroItem domain) {
        RetroItemEntity entity = RetroItemEntity.builder()
                .retroId(domain.getRetroId())
                .category(domain.getCategory())
                .content(domain.getContent())
                .votes(domain.getVotes())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private RetroItem toItemDomain(RetroItemEntity entity) {
        return RetroItem.builder()
                .id(entity.getId().toString())
                .retroId(entity.getRetroId())
                .category(entity.getCategory())
                .content(entity.getContent())
                .votes(entity.getVotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- ActionItem Mapping ---

    private ActionItemEntity toActionItemEntity(ActionItem domain) {
        ActionItemEntity entity = ActionItemEntity.builder()
                .retroId(domain.getRetroId())
                .description(domain.getDescription())
                .assignee(domain.getAssignee())
                .dueDate(domain.getDueDate())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private ActionItem toActionItemDomain(ActionItemEntity entity) {
        return ActionItem.builder()
                .id(entity.getId().toString())
                .retroId(entity.getRetroId())
                .description(entity.getDescription())
                .assignee(entity.getAssignee())
                .dueDate(entity.getDueDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
