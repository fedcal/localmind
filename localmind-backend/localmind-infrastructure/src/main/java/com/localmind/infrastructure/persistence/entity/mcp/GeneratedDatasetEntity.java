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
 * JPA entity for the mcp_generated_datasets table.
 * Stores results of data-mock-generator operations (generateMockData, generateMockJson, generateMockCsv).
 */
@Entity
@Table(name = "mcp_generated_datasets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedDatasetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String format;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "schema_json", columnDefinition = "LONGTEXT")
    private String schemaJson;

    @Column(name = "sample_data", columnDefinition = "LONGTEXT")
    private String sampleData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
