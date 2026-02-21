package com.localmind.shared.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void testBuilder_setsAllFields() {
        Instant now = Instant.now();

        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Document not found: abc-123")
                .path("/api/v1/documents/abc-123")
                .timestamp(now)
                .build();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).isEqualTo("Document not found: abc-123");
        assertThat(response.getPath()).isEqualTo("/api/v1/documents/abc-123");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

    @Test
    void testDefaultTimestamp() {
        Instant before = Instant.now();

        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Unexpected error")
                .path("/api/v1/chat")
                .build();

        Instant after = Instant.now();

        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBetween(before, after);
    }
}
