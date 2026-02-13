package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a Docker Compose / Dockerfile analysis result.
 * Stores the original content, the type of analysis, and the JSON result.
 */
public class ComposeAnalysis {

    private final String id;
    private final String contentType;
    private final int serviceCount;
    private final String resultJson;
    private final Instant createdAt;

    private ComposeAnalysis(Builder builder) {
        this.id = builder.id;
        this.contentType = builder.contentType;
        this.serviceCount = builder.serviceCount;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public int getServiceCount() {
        return serviceCount;
    }

    public String getResultJson() {
        return resultJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String contentType;
        private int serviceCount;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder serviceCount(int serviceCount) {
            this.serviceCount = serviceCount;
            return this;
        }

        public Builder resultJson(String resultJson) {
            this.resultJson = resultJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ComposeAnalysis build() {
            return new ComposeAnalysis(this);
        }
    }
}
