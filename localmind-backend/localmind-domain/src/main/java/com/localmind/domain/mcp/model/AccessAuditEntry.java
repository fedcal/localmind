package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an access audit log entry.
 * Records each access check performed against policies.
 */
public class AccessAuditEntry {

    private final String id;
    private final String userId;
    private final String server;
    private final String tool;
    private final boolean allowed;
    private final String policyId;
    private final Instant createdAt;

    private AccessAuditEntry(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.server = builder.server;
        this.tool = builder.tool;
        this.allowed = builder.allowed;
        this.policyId = builder.policyId;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getServer() {
        return server;
    }

    public String getTool() {
        return tool;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getPolicyId() {
        return policyId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String server;
        private String tool;
        private boolean allowed;
        private String policyId;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder server(String server) {
            this.server = server;
            return this;
        }

        public Builder tool(String tool) {
            this.tool = tool;
            return this;
        }

        public Builder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }

        public Builder policyId(String policyId) {
            this.policyId = policyId;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccessAuditEntry build() {
            return new AccessAuditEntry(this);
        }
    }
}
