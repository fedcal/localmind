package com.localmind.domain.document.port.out;

import com.localmind.domain.document.model.DocumentStats;

import java.util.List;
import java.util.Map;

/**
 * Porta di uscita per il modello di lettura dei documenti (CQRS).
 * Fornisce query ottimizzate sulle statistiche pre-calcolate.
 *
 * Output port for the document read model (CQRS).
 * Provides optimized queries on pre-calculated statistics.
 */
public interface DocumentReadRepository {

    List<DocumentStats> findAllWithStats();

    Map<String, Long> getTypeDistribution();

    Map<String, Long> getStatusDistribution();

    long countAll();
}
