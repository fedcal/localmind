package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a daily standup note.
 * Stores what was done yesterday, what is planned for today,
 * any blockers, and the standup date.
 */
public class StandupNote {

    private final String id;
    private final String yesterday;
    private final String today;
    private final String blockers;
    private final String standupDate;
    private final Instant createdAt;

    private StandupNote(Builder builder) {
        this.id = builder.id;
        this.yesterday = builder.yesterday;
        this.today = builder.today;
        this.blockers = builder.blockers;
        this.standupDate = builder.standupDate;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getYesterday() {
        return yesterday;
    }

    public String getToday() {
        return today;
    }

    public String getBlockers() {
        return blockers;
    }

    public String getStandupDate() {
        return standupDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String yesterday;
        private String today;
        private String blockers;
        private String standupDate;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder yesterday(String yesterday) {
            this.yesterday = yesterday;
            return this;
        }

        public Builder today(String today) {
            this.today = today;
            return this;
        }

        public Builder blockers(String blockers) {
            this.blockers = blockers;
            return this;
        }

        public Builder standupDate(String standupDate) {
            this.standupDate = standupDate;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public StandupNote build() {
            return new StandupNote(this);
        }
    }
}
