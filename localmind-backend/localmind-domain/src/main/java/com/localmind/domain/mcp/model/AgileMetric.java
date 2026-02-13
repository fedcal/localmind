package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an agile metric calculation result.
 * Stores the metric type (velocity, burndown, cycle-time, forecast, risk, correlation),
 * an optional sprint reference, and the JSON result.
 */
public class AgileMetric {

    private final String id;
    private final String metricType;
    private final String sprintRef;
    private final String resultJson;
    private final Instant createdAt;

    private AgileMetric(Builder builder) {
        this.id = builder.id;
        this.metricType = builder.metricType;
        this.sprintRef = builder.sprintRef;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getMetricType() {
        return metricType;
    }

    public String getSprintRef() {
        return sprintRef;
    }

    public String getResultJson() {
        return resultJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String metricType;
        private String sprintRef;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder metricType(String metricType) {
            this.metricType = metricType;
            return this;
        }

        public Builder sprintRef(String sprintRef) {
            this.sprintRef = sprintRef;
            return this;
        }

        public Builder resultJson(String resultJson) {
            this.resultJson = resultJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AgileMetric build() {
            return new AgileMetric(this);
        }
    }
}
