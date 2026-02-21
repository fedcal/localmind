package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an architectural or technical decision record.
 * Supports statuses: proposed, accepted, deprecated, superseded.
 */
public class DecisionRecord {

    private final String id;
    private final String title;
    private final String context;
    private final String decision;
    private final String alternativesJson;
    private final String consequences;
    private final String status;
    private final String relatedTicketsJson;
    private final Instant createdAt;

    private DecisionRecord(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.context = builder.context;
        this.decision = builder.decision;
        this.alternativesJson = builder.alternativesJson;
        this.consequences = builder.consequences;
        this.status = builder.status;
        this.relatedTicketsJson = builder.relatedTicketsJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContext() {
        return context;
    }

    public String getDecision() {
        return decision;
    }

    public String getAlternativesJson() {
        return alternativesJson;
    }

    public String getConsequences() {
        return consequences;
    }

    public String getStatus() {
        return status;
    }

    public String getRelatedTicketsJson() {
        return relatedTicketsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private String context;
        private String decision;
        private String alternativesJson;
        private String consequences;
        private String status;
        private String relatedTicketsJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder context(String context) {
            this.context = context;
            return this;
        }

        public Builder decision(String decision) {
            this.decision = decision;
            return this;
        }

        public Builder alternativesJson(String alternativesJson) {
            this.alternativesJson = alternativesJson;
            return this;
        }

        public Builder consequences(String consequences) {
            this.consequences = consequences;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder relatedTicketsJson(String relatedTicketsJson) {
            this.relatedTicketsJson = relatedTicketsJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DecisionRecord build() {
            return new DecisionRecord(this);
        }
    }
}
