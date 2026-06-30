package com.localmind.api.marketplace.dto;

import java.time.Instant;

public record MarketplaceAgentDto(
        String id,
        String name,
        String description,
        String author,
        String version,
        String category,
        String downloadUrl,
        int downloadCount,
        double avgRating,
        Instant publishedAt,
        Instant createdAt
) {}
