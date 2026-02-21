package com.localmind.infrastructure.finetuning.persistence.entity;

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
@Table(name = "training_datasets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDatasetEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "document_ids", columnDefinition = "TEXT")
    private String documentIds;

    @Column(name = "sample_count", nullable = false)
    private int sampleCount;

    @Column(length = 50)
    private String format;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
