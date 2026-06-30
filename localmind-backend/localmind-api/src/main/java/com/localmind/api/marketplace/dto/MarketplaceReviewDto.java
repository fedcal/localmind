package com.localmind.api.marketplace.dto;

import java.time.Instant;

public record MarketplaceReviewDto(
        String id,
        String agentId,
        int rating,
        String comment,
        Instant createdAt
) {}
