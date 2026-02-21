package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a quality gate definition.
 * Contains a set of checks (metric/operator/threshold) that must all pass.
 */
public class QualityGate {

    private final String id;
    private final String name;
    private final String projectName;
    private final String checksJson;
    private final Instant createdAt;

    private QualityGate(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.projectName = builder.projectName;
        this.checksJson = builder.checksJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getChecksJson() {
        return checksJson;
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
        private String projectName;
        private String checksJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder checksJson(String checksJson) {
            this.checksJson = checksJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public QualityGate build() {
            return new QualityGate(this);
        }
    }
}
