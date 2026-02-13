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
@Table(name = "mcp_access_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 200)
    private String userId;

    @Column(name = "server", nullable = false, length = 200)
    private String server;

    @Column(name = "tool", nullable = false, length = 200)
    private String tool;

    @Column(name = "allowed", nullable = false)
    private boolean allowed;

    @Column(name = "policy_id", length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String policyId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
