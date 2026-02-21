package com.localmind.domain.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Modello di lettura ottimizzato per le statistiche dei documenti (CQRS).
 * Contiene dati pre-calcolati come il conteggio dei chunk.
 *
 * Optimized read model for document statistics (CQRS).
 * Contains pre-calculated data such as chunk count.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStats {
    private String id;
    private String title;
    private String status;
    private int chunkCount;
    private long totalSize;
    private String mimeType;
    private Double ocrConfidence;
    private Instant createdAt;
}
