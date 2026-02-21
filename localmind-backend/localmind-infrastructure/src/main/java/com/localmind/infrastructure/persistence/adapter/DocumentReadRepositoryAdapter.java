package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.DocumentStats;
import com.localmind.domain.document.port.out.DocumentReadRepository;
import com.localmind.infrastructure.persistence.entity.DocumentStatsCacheEntity;
import com.localmind.infrastructure.persistence.repository.DocumentStatsCacheJpaRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter per il modello di lettura dei documenti (CQRS).
 * Implementa la porta di dominio utilizzando il repository JPA dedicato.
 *
 * Adapter for the document read model (CQRS).
 * Implements the domain port using the dedicated JPA repository.
 */
@Component
public class DocumentReadRepositoryAdapter implements DocumentReadRepository {

    private final DocumentStatsCacheJpaRepository jpaRepository;

    public DocumentReadRepositoryAdapter(DocumentStatsCacheJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<DocumentStats> findAllWithStats() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getTypeDistribution() {
        return jpaRepository.getTypeDistribution().stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "unknown",
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Long> getStatusDistribution() {
        return jpaRepository.getStatusDistribution().stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "unknown",
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    private DocumentStats toDomain(DocumentStatsCacheEntity entity) {
        return DocumentStats.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .chunkCount(entity.getChunkCount())
                .totalSize(entity.getTotalSize())
                .mimeType(entity.getMimeType())
                .ocrConfidence(entity.getOcrConfidence())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
