package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a project budget.
 * Stores the project name, total budget amount and currency.
 */
public class ProjectBudget {

    private final String id;
    private final String projectName;
    private final double totalBudget;
    private final String currency;
    private final Instant createdAt;

    private ProjectBudget(Builder builder) {
        this.id = builder.id;
        this.projectName = builder.projectName;
        this.totalBudget = builder.totalBudget;
        this.currency = builder.currency;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String projectName;
        private double totalBudget;
        private String currency;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder totalBudget(double totalBudget) {
            this.totalBudget = totalBudget;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ProjectBudget build() {
            return new ProjectBudget(this);
        }
    }
}
