package com.localmind.api.marketplace.dto;

public record AddReviewRequest(
        int rating,
        String comment
) {}
