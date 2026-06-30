package com.localmind.domain.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceAgent {
    private String id;
    private String name;
    private String description;
    private String author;
    private String version;
    private AgentCategory category;
    private String downloadUrl;
    @Builder.Default
    private int downloadCount = 0;
    @Builder.Default
    private double avgRating = 0.0;
    private Instant publishedAt;
    private Instant createdAt;
}
