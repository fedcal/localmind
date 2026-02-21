package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a dashboard overview (in-memory only, no persistence).
 * Aggregates sprint, velocity, budget summaries.
 */
public class DashboardOverview {

    private final String sprintSummary;
    private final String velocitySummary;
    private final String budgetSummary;
    private final Instant generatedAt;

    private DashboardOverview(Builder builder) {
        this.sprintSummary = builder.sprintSummary;
        this.velocitySummary = builder.velocitySummary;
        this.budgetSummary = builder.budgetSummary;
        this.generatedAt = builder.generatedAt;
    }

    public String getSprintSummary() {
        return sprintSummary;
    }

    public String getVelocitySummary() {
        return velocitySummary;
    }

    public String getBudgetSummary() {
        return budgetSummary;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sprintSummary;
        private String velocitySummary;
        private String budgetSummary;
        private Instant generatedAt;

        public Builder sprintSummary(String sprintSummary) {
            this.sprintSummary = sprintSummary;
            return this;
        }

        public Builder velocitySummary(String velocitySummary) {
            this.velocitySummary = velocitySummary;
            return this;
        }

        public Builder budgetSummary(String budgetSummary) {
            this.budgetSummary = budgetSummary;
            return this;
        }

        public Builder generatedAt(Instant generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public DashboardOverview build() {
            return new DashboardOverview(this);
        }
    }
}
