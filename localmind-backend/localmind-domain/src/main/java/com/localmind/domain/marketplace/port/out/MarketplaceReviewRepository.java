package com.localmind.domain.marketplace.port.out;

import com.localmind.domain.marketplace.model.MarketplaceReview;

import java.util.List;

public interface MarketplaceReviewRepository {

    MarketplaceReview save(MarketplaceReview review);

    List<MarketplaceReview> findByAgentId(String agentId);

    double calculateAverageRating(String agentId);
}
