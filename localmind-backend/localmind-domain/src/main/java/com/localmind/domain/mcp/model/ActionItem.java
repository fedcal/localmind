package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an action item derived from a retrospective.
 * Tracks who is responsible, when it is due, and its current status.
 */
public class ActionItem {

    private final String id;
    private final String retroId;
    private final String description;
    private final String assignee;
    private final String dueDate;
    private final String status;
    private final Instant createdAt;

    private ActionItem(Builder builder) {
        this.id = builder.id;
        this.retroId = builder.retroId;
        this.description = builder.description;
        this.assignee = builder.assignee;
        this.dueDate = builder.dueDate;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRetroId() {
        return retroId;
    }

    public String getDescription() {
        return description;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String retroId;
        private String description;
        private String assignee;
        private String dueDate;
        private String status;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder retroId(String retroId) {
            this.retroId = retroId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder assignee(String assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder dueDate(String dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ActionItem build() {
            return new ActionItem(this);
        }
    }
}
