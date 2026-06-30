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
 * Entita JPA per il modello di lettura dei riepiloghi conversazioni (CQRS).
 * JPA entity for the conversation summaries read model (CQRS).
 */
@Entity
@Table(name = "conversation_summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(length = 200)
    private String title;

    @Column(name = "message_count")
    private int messageCount;

    @Column(name = "last_message_preview", length = 500)
    private String lastMessagePreview;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
