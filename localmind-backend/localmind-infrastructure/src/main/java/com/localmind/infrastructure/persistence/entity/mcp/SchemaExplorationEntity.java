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
 * JPA entity for the mcp_schema_explorations table.
 * Stores results of database schema exploration executed by the db-schema-explorer tool group.
 */
@Entity
@Table(name = "mcp_schema_explorations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaExplorationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "jdbc_url", nullable = false, length = 500)
    private String jdbcUrl;

    @Column(name = "table_count", nullable = false)
    private int tableCount;

    @Column(name = "result", columnDefinition = "LONGTEXT")
    private String result;

    @Column(name = "explored_at", nullable = false)
    private Instant exploredAt;

    @PrePersist
    protected void onCreate() {
        if (exploredAt == null) {
            exploredAt = Instant.now();
        }
    }
}
