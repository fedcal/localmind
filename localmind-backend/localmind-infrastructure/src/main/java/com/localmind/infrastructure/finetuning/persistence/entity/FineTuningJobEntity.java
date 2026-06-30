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
@Table(name = "finetuning_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineTuningJobEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "base_model", nullable = false, length = 100)
    private String baseModel;

    @Column(nullable = false, length = 20)
    private String technique;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int progress;

    @Column(name = "dataset_id", length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID datasetId;

    @Column(name = "output_model_name", length = 100)
    private String outputModelName;

    @Column(columnDefinition = "TEXT")
    private String hyperparameters;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
