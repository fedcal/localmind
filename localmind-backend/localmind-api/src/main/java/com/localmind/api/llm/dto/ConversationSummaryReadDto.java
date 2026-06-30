package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO per il modello di lettura CQRS dei riepiloghi conversazioni.
 * Contiene dati pre-calcolati dal read model.
 *
 * DTO for the CQRS read model of conversation summaries.
 * Contains pre-calculated data from the read model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryReadDto {
    private String id;
    private String title;
    private int messageCount;
    private String lastMessagePreview;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
