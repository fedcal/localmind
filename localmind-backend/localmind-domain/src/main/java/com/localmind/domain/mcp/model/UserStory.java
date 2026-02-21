package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a User Story in the Scrum board.
 * Contains story details, acceptance criteria, points, and priority.
 */
public class UserStory {

    private final String id;
    private final String title;
    private final String description;
    private final String acceptanceCriteriaJson;
    private final int storyPoints;
    private final String priority;
    private final String sprintId;
    private final Instant createdAt;

    private UserStory(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.acceptanceCriteriaJson = builder.acceptanceCriteriaJson;
        this.storyPoints = builder.storyPoints;
        this.priority = builder.priority;
        this.sprintId = builder.sprintId;
        this.createdAt = builder.createdAt;
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

    public String getAcceptanceCriteriaJson() {
        return acceptanceCriteriaJson;
    }

    public int getStoryPoints() {
        return storyPoints;
    }

    public String getPriority() {
        return priority;
    }

    public String getSprintId() {
        return sprintId;
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
        private String description;
        private String acceptanceCriteriaJson;
        private int storyPoints;
        private String priority;
        private String sprintId;
        private Instant createdAt;

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

        public Builder acceptanceCriteriaJson(String acceptanceCriteriaJson) {
            this.acceptanceCriteriaJson = acceptanceCriteriaJson;
            return this;
        }

        public Builder storyPoints(int storyPoints) {
            this.storyPoints = storyPoints;
            return this;
        }

        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder sprintId(String sprintId) {
            this.sprintId = sprintId;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserStory build() {
            return new UserStory(this);
        }
    }
}
