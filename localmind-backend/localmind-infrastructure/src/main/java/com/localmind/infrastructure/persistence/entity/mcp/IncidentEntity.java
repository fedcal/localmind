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
@Table(name = "mcp_incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "affected_systems_json", columnDefinition = "LONGTEXT")
    private String affectedSystemsJson;

    @Column(name = "timeline_json", columnDefinition = "LONGTEXT")
    private String timelineJson;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
