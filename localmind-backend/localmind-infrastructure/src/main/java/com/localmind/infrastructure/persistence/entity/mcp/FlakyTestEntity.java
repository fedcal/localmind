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
 * JPA entity for the mcp_flaky_tests table.
 * Stores flaky test detection results for the cicd-monitor tool group.
 */
@Entity
@Table(name = "mcp_flaky_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlakyTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(length = 200)
    private String repo;

    @Column(name = "test_name", nullable = false, length = 500)
    private String testName;

    @Column(length = 200)
    private String workflow;

    @Column(name = "pass_count", nullable = false)
    private int passCount;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(name = "flakiness_rate", nullable = false)
    private double flakinessRate;

    @Column(name = "detected_at")
    private Instant detectedAt;

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = Instant.now();
        }
    }
}
