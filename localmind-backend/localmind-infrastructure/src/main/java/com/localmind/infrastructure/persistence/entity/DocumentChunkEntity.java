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

@Entity
@Table(name = "document_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "document_id", nullable = false, columnDefinition = "CHAR(36)")
    private String documentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
