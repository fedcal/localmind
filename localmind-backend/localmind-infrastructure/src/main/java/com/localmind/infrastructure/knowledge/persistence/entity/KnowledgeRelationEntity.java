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
@Table(name = "knowledge_relations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelationEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "from_entity_id", nullable = false, columnDefinition = "char(36)")
    private String fromEntityId;

    @Column(name = "to_entity_id", nullable = false, columnDefinition = "char(36)")
    private String toEntityId;

    @Column(name = "relation_type", nullable = false, length = 30)
    private String relationType;

    @Column(columnDefinition = "TEXT")
    private String properties;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
