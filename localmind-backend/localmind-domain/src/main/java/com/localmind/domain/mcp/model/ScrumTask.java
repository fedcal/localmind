package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a Task in the Scrum board.
 * Tasks belong to a User Story and track assignee and status.
 */
public class ScrumTask {

    private final String id;
    private final String title;
    private final String description;
    private final String storyId;
    private final String assignee;
    private final String status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ScrumTask(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.storyId = builder.storyId;
        this.assignee = builder.assignee;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private String description;
        private String storyId;
        private String assignee;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder storyId(String storyId) {
            this.storyId = storyId;
            return this;
        }

        public Builder assignee(String assignee) {
            this.assignee = assignee;
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

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ScrumTask build() {
            return new ScrumTask(this);
        }
    }
}
