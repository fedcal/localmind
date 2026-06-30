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
public class MarketplaceReview {
    private String id;
    private String agentId;
    private int rating;
    private String comment;
    private Instant createdAt;
}
