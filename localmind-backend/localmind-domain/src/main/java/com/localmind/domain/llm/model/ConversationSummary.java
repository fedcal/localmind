package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Modello di lettura ottimizzato per i riepiloghi delle conversazioni (CQRS).
 * Non contiene i messaggi completi, solo le informazioni di riepilogo.
 *
 * Optimized read model for conversation summaries (CQRS).
 * Does not contain full messages, only summary information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSummary {
    private String id;
    private String title;
    private int messageCount;
    private String lastMessagePreview;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
