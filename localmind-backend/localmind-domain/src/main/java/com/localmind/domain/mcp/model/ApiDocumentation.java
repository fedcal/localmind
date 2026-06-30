package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an API documentation analysis result.
 * Stores the documentation type, file path, and the JSON result of the analysis.
 */
public class ApiDocumentation {

    private final String id;
    private final String documentationType;
    private final String filePath;
    private final String resultJson;
    private final Instant createdAt;

    private ApiDocumentation(Builder builder) {
        this.id = builder.id;
        this.documentationType = builder.documentationType;
        this.filePath = builder.filePath;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDocumentationType() {
        return documentationType;
    }

    public String getFilePath() {
        return filePath;
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
        private String documentationType;
        private String filePath;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder documentationType(String documentationType) {
            this.documentationType = documentationType;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
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

        public ApiDocumentation build() {
            return new ApiDocumentation(this);
        }
    }
}
