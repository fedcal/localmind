package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a sprint retrospective session.
 * Stores the sprint identifier, the retrospective format, and when it was created.
 */
public class Retrospective {

    private final String id;
    private final String sprintId;
    private final String format;
    private final Instant createdAt;

    private Retrospective(Builder builder) {
        this.id = builder.id;
        this.sprintId = builder.sprintId;
        this.format = builder.format;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getSprintId() {
        return sprintId;
    }

    public String getFormat() {
        return format;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String sprintId;
        private String format;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder sprintId(String sprintId) {
            this.sprintId = sprintId;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Retrospective build() {
            return new Retrospective(this);
        }
    }
}
