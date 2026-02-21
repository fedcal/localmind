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
@Table(name = "mcp_decisions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "context", nullable = false, length = 2000)
    private String context;

    @Column(name = "decision_text", columnDefinition = "TEXT", nullable = false)
    private String decisionText;

    @Column(name = "alternatives_json", columnDefinition = "LONGTEXT")
    private String alternativesJson;

    @Column(name = "consequences", columnDefinition = "TEXT")
    private String consequences;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "related_tickets_json", columnDefinition = "LONGTEXT")
    private String relatedTicketsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
