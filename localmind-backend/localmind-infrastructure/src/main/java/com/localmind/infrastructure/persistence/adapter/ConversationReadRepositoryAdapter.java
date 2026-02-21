package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.llm.model.ConversationSummary;
import com.localmind.domain.llm.port.out.ConversationReadRepository;
import com.localmind.infrastructure.persistence.entity.ConversationSummaryEntity;
import com.localmind.infrastructure.persistence.repository.ConversationSummaryJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter per il modello di lettura delle conversazioni (CQRS).
 * Implementa la porta di dominio utilizzando il repository JPA dedicato.
 *
 * Adapter for the conversation read model (CQRS).
 * Implements the domain port using the dedicated JPA repository.
 */
@Component
public class ConversationReadRepositoryAdapter implements ConversationReadRepository {

    private final ConversationSummaryJpaRepository jpaRepository;

    public ConversationReadRepositoryAdapter(ConversationSummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ConversationSummary> findRecentSummaries(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findAllByOrderByUpdatedAtDesc(pageable).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationSummary> searchSummaries(String query) {
        return jpaRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ConversationSummary> findSummaryById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    private ConversationSummary toDomain(ConversationSummaryEntity entity) {
        List<String> tagsList = entity.getTags() != null && !entity.getTags().isBlank()
                ? Arrays.asList(entity.getTags().split(","))
                : Collections.emptyList();

        return ConversationSummary.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .messageCount(entity.getMessageCount())
                .lastMessagePreview(entity.getLastMessagePreview())
                .tags(tagsList)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
