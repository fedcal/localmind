package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing the cost of a specific feature in a project.
 * Stores hours spent, hourly rate, total cost, description and currency.
 */
public class FeatureCost {

    private final String id;
    private final String featureId;
    private final String projectName;
    private final double hoursSpent;
    private final double hourlyRate;
    private final double totalCost;
    private final String description;
    private final String currency;
    private final Instant createdAt;

    private FeatureCost(Builder builder) {
        this.id = builder.id;
        this.featureId = builder.featureId;
        this.projectName = builder.projectName;
        this.hoursSpent = builder.hoursSpent;
        this.hourlyRate = builder.hourlyRate;
        this.totalCost = builder.totalCost;
        this.description = builder.description;
        this.currency = builder.currency;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getProjectName() {
        return projectName;
    }

    public double getHoursSpent() {
        return hoursSpent;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public String getDescription() {
        return description;
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
        private String featureId;
        private String projectName;
        private double hoursSpent;
        private double hourlyRate;
        private double totalCost;
        private String description;
        private String currency;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder featureId(String featureId) {
            this.featureId = featureId;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder hoursSpent(double hoursSpent) {
            this.hoursSpent = hoursSpent;
            return this;
        }

        public Builder hourlyRate(double hourlyRate) {
            this.hourlyRate = hourlyRate;
            return this;
        }

        public Builder totalCost(double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public FeatureCost build() {
            return new FeatureCost(this);
        }
    }
}
