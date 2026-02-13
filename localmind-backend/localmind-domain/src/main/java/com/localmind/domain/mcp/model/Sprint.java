package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a Sprint in the Scrum board.
 * Contains sprint metadata such as name, dates, and goals.
 */
public class Sprint {

    private final String id;
    private final String name;
    private final String startDate;
    private final String endDate;
    private final String goalsJson;
    private final Instant createdAt;

    private Sprint(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.goalsJson = builder.goalsJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getGoalsJson() {
        return goalsJson;
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
        private String startDate;
        private String endDate;
        private String goalsJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder startDate(String startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(String endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder goalsJson(String goalsJson) {
            this.goalsJson = goalsJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Sprint build() {
            return new Sprint(this);
        }
    }
}
