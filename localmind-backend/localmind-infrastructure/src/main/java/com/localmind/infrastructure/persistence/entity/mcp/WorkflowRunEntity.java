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

@Entity
@Table(name = "mcp_workflow_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "workflow_id", nullable = false, columnDefinition = "CHAR(36)")
    private String workflowId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "payload_json", columnDefinition = "LONGTEXT")
    private String payloadJson;

    @Column(name = "step_results_json", columnDefinition = "LONGTEXT")
    private String stepResultsJson;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }
}
