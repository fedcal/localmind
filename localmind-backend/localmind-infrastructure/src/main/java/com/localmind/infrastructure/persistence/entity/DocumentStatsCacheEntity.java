package com.localmind.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Entita JPA per il modello di lettura delle statistiche documenti (CQRS).
 * JPA entity for the document statistics read model (CQRS).
 */
@Entity
@Table(name = "document_stats_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatsCacheEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(length = 500)
    private String title;

    @Column(length = 20)
    private String status;

    @Column(name = "chunk_count")
    private int chunkCount;

    @Column(name = "total_size")
    private long totalSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "ocr_confidence")
    private Double ocrConfidence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
