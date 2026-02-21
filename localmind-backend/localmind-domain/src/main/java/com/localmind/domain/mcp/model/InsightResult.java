package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an insight analysis result.
 * Stores the original question and the generated insight as JSON.
 */
public class InsightResult {

    private final String id;
    private final String question;
    private final String resultJson;
    private final Instant createdAt;

    private InsightResult(Builder builder) {
        this.id = builder.id;
        this.question = builder.question;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
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
        private String question;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder question(String question) {
            this.question = question;
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

        public InsightResult build() {
            return new InsightResult(this);
        }
    }
}
