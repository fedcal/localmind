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
@Table(name = "mcp_gate_evaluations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "gate_id", nullable = false, columnDefinition = "CHAR(36)")
    private String gateId;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "metrics_json", columnDefinition = "LONGTEXT")
    private String metricsJson;

    @Column(name = "results_json", columnDefinition = "LONGTEXT")
    private String resultsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
