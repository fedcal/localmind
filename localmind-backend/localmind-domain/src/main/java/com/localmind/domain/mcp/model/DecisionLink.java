package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a link between a decision record and an external artifact.
 * Link types: ticket, commit, impact, related.
 */
public class DecisionLink {

    private final String id;
    private final String decisionId;
    private final String linkType;
    private final String targetId;
    private final String description;
    private final Instant createdAt;

    private DecisionLink(Builder builder) {
        this.id = builder.id;
        this.decisionId = builder.decisionId;
        this.linkType = builder.linkType;
        this.targetId = builder.targetId;
        this.description = builder.description;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String decisionId;
        private String linkType;
        private String targetId;
        private String description;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder decisionId(String decisionId) {
            this.decisionId = decisionId;
            return this;
        }

        public Builder linkType(String linkType) {
            this.linkType = linkType;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DecisionLink build() {
            return new DecisionLink(this);
        }
    }
}
