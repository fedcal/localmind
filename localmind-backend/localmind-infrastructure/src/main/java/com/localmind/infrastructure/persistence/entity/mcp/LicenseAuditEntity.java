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
 * JPA entity for the mcp_license_audits table.
 * Stores results of license audits executed by the dependency-manager tool group.
 */
@Entity
@Table(name = "mcp_license_audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "project_path", nullable = false, length = 500)
    private String projectPath;

    @Column(name = "project_type", nullable = false, length = 20)
    private String projectType;

    @Column(name = "total_checked", nullable = false)
    private int totalChecked;

    @Column(name = "copyleft_count", nullable = false)
    private int copyleftCount;

    @Column(name = "result", columnDefinition = "LONGTEXT")
    private String result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
