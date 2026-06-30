package com.localmind.api.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record IndexTextRequest(
        @NotBlank String text,
        String sourceDocumentId
) {}
