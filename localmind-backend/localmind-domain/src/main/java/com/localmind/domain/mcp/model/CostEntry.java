package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a cost entry for a project.
 * Stores the category, amount, description and entry date.
 */
public class CostEntry {

    private final String id;
    private final String projectName;
    private final String category;
    private final double amount;
    private final String description;
    private final String entryDate;
    private final Instant createdAt;

    private CostEntry(Builder builder) {
        this.id = builder.id;
        this.projectName = builder.projectName;
        this.category = builder.category;
        this.amount = builder.amount;
        this.description = builder.description;
        this.entryDate = builder.entryDate;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getEntryDate() {
        return entryDate;
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
        private String category;
        private double amount;
        private String description;
        private String entryDate;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CostEntry build() {
            return new CostEntry(this);
        }
    }
}
