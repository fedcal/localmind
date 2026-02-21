package com.localmind.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO per il modello di lettura CQRS delle statistiche documenti.
 * Contiene dati pre-calcolati dal read model.
 *
 * DTO for the CQRS read model of document statistics.
 * Contains pre-calculated data from the read model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatsDto {
    private String id;
    private String title;
    private String status;
    private int chunkCount;
    private long totalSize;
    private String mimeType;
    private Double ocrConfidence;
    private Instant createdAt;
}
