package com.localmind.infrastructure.persistence.entity.mcp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the mcp_pipeline_runs table.
 * Stores CI/CD pipeline run data for the cicd-monitor tool group.
 */
@Entity
@Table(name = "mcp_pipeline_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(length = 200)
    private String owner;

    @Column(length = 200)
    private String repo;

    @Column(name = "run_id", length = 100)
    private String runId;

    @Column(length = 500)
    private String title;

    @Column(length = 200)
    private String branch;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String conclusion;

    @Column(length = 200)
    private String workflow;

    @Column(length = 500)
    private String url;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
