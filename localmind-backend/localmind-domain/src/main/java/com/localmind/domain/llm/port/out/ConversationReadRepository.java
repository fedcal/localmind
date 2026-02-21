package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.ConversationSummary;

import java.util.List;
import java.util.Optional;

/**
 * Porta di uscita per il modello di lettura delle conversazioni (CQRS).
 * Fornisce query ottimizzate sui dati pre-calcolati.
 *
 * Output port for the conversation read model (CQRS).
 * Provides optimized queries on pre-calculated data.
 */
public interface ConversationReadRepository {

    List<ConversationSummary> findRecentSummaries(int limit);

    List<ConversationSummary> searchSummaries(String query);

    Optional<ConversationSummary> findSummaryById(String id);

    long countAll();
}
