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
 * JPA entity for the mcp_code_reviews table.
 * Stores results of code review analyses executed by the code-review tool group.
 */
@Entity
@Table(name = "mcp_code_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "review_type", nullable = false, length = 50)
    private String reviewType;

    @Column(name = "issues_found", nullable = false)
    private int issuesFound;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

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
