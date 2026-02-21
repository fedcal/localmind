package com.localmind.api.marketplace.dto;

public record PublishAgentRequest(
        String name,
        String description,
        String author,
        String version,
        String category,
        String downloadUrl
) {}
