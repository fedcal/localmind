package com.localmind.infrastructure.marketplace.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketplace_agents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceAgentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String author;

    @Column(length = 20)
    private String version;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(length = 500)
    private String downloadUrl;

    @Builder.Default
    @Column(nullable = false)
    private int downloadCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private double avgRating = 0.0;

    private Instant publishedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
