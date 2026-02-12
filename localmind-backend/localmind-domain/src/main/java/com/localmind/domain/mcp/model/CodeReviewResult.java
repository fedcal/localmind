package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing the result of a code review analysis.
 * Used by the code-review tool group (analyzeDiff, checkComplexity, suggestImprovements).
 */
public class CodeReviewResult {

    private final String id;
    private final String reviewType;
    private final int issuesFound;
    private final String suggestionsJson;
    private final String resultJson;
    private final Instant createdAt;

    private CodeReviewResult(Builder builder) {
        this.id = builder.id;
        this.reviewType = builder.reviewType;
        this.issuesFound = builder.issuesFound;
        this.suggestionsJson = builder.suggestionsJson;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getReviewType() {
        return reviewType;
    }

    public int getIssuesFound() {
        return issuesFound;
    }

    public String getSuggestionsJson() {
        return suggestionsJson;
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
        private String reviewType;
        private int issuesFound;
        private String suggestionsJson;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder reviewType(String reviewType) {
            this.reviewType = reviewType;
            return this;
        }

        public Builder issuesFound(int issuesFound) {
            this.issuesFound = issuesFound;
            return this;
        }

        public Builder suggestionsJson(String suggestionsJson) {
            this.suggestionsJson = suggestionsJson;
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

        public CodeReviewResult build() {
            return new CodeReviewResult(this);
        }
    }
}
