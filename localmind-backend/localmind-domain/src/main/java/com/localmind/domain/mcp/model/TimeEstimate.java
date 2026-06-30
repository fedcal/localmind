package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a time estimate for a task.
 * Used by the time-tracking tool group to compare estimated vs actual time.
 */
public class TimeEstimate {

    private final String id;
    private final String taskId;
    private final int estimateMinutes;
    private final String description;
    private final Instant createdAt;

    private TimeEstimate(Builder builder) {
        this.id = builder.id;
        this.taskId = builder.taskId;
        this.estimateMinutes = builder.estimateMinutes;
        this.description = builder.description;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getEstimateMinutes() {
        return estimateMinutes;
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
        private String taskId;
        private int estimateMinutes;
        private String description;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder estimateMinutes(int estimateMinutes) {
            this.estimateMinutes = estimateMinutes;
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

        public TimeEstimate build() {
            return new TimeEstimate(this);
        }
    }
}
