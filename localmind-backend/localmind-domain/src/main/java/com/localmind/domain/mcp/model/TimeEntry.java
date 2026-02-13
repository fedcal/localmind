package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a time tracking entry.
 * Used by the time-tracking tool group (startTimer, stopTimer, logTime, getTimesheet, detectAnomalies, estimateVsActual).
 */
public class TimeEntry {

    private final String id;
    private final String taskId;
    private final String userId;
    private final int durationMinutes;
    private final String description;
    private final String entryDate;
    private final Instant timerStartedAt;
    private final Instant createdAt;

    private TimeEntry(Builder builder) {
        this.id = builder.id;
        this.taskId = builder.taskId;
        this.userId = builder.userId;
        this.durationMinutes = builder.durationMinutes;
        this.description = builder.description;
        this.entryDate = builder.entryDate;
        this.timerStartedAt = builder.timerStartedAt;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getUserId() {
        return userId;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public Instant getTimerStartedAt() {
        return timerStartedAt;
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
        private String userId;
        private int durationMinutes;
        private String description;
        private String entryDate;
        private Instant timerStartedAt;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder durationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder entryDate(String entryDate) {
            this.entryDate = entryDate;
            return this;
        }

        public Builder timerStartedAt(Instant timerStartedAt) {
            this.timerStartedAt = timerStartedAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TimeEntry build() {
            return new TimeEntry(this);
        }
    }
}
