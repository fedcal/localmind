package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.ConversationSummaryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository JPA per il modello di lettura dei riepiloghi conversazioni (CQRS).
 * JPA repository for the conversation summaries read model (CQRS).
 */
@Repository
public interface ConversationSummaryJpaRepository extends JpaRepository<ConversationSummaryEntity, UUID> {

    List<ConversationSummaryEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<ConversationSummaryEntity> findByTitleContainingIgnoreCase(String query);
}
