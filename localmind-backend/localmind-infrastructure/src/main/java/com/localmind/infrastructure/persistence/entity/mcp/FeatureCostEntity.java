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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the mcp_feature_costs table.
 * Stores per-feature cost data for the project-economics tool group.
 */
@Entity
@Table(name = "mcp_feature_costs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureCostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "feature_id", nullable = false, length = 200)
    private String featureId;

    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    @Column(name = "hours_spent", nullable = false, precision = 10, scale = 2)
    private BigDecimal hoursSpent;

    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "total_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
