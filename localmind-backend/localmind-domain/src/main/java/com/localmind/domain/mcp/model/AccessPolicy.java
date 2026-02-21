package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an access control policy.
 * Defines allow/deny rules for MCP tool access.
 */
public class AccessPolicy {

    private final String id;
    private final String name;
    private final String effect;
    private final String rulesJson;
    private final Instant createdAt;

    private AccessPolicy(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.effect = builder.effect;
        this.rulesJson = builder.rulesJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEffect() {
        return effect;
    }

    public String getRulesJson() {
        return rulesJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String effect;
        private String rulesJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder effect(String effect) {
            this.effect = effect;
            return this;
        }

        public Builder rulesJson(String rulesJson) {
            this.rulesJson = rulesJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccessPolicy build() {
            return new AccessPolicy(this);
        }
    }
}
