package com.localmind.infrastructure.knowledge.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_entities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEntityEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @Column(columnDefinition = "TEXT")
    private String properties;

    @Column(name = "source_document_id", length = 36)
    private String sourceDocumentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
