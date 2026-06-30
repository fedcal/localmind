package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing the result of evaluating a quality gate.
 * Stores whether all checks passed, the input metrics and the per-check results.
 */
public class GateEvaluation {

    private final String id;
    private final String gateId;
    private final boolean passed;
    private final String metricsJson;
    private final String resultsJson;
    private final Instant createdAt;

    private GateEvaluation(Builder builder) {
        this.id = builder.id;
        this.gateId = builder.gateId;
        this.passed = builder.passed;
        this.metricsJson = builder.metricsJson;
        this.resultsJson = builder.resultsJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getGateId() {
        return gateId;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMetricsJson() {
        return metricsJson;
    }

    public String getResultsJson() {
        return resultsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String gateId;
        private boolean passed;
        private String metricsJson;
        private String resultsJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder gateId(String gateId) {
            this.gateId = gateId;
            return this;
        }

        public Builder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public Builder metricsJson(String metricsJson) {
            this.metricsJson = metricsJson;
            return this;
        }

        public Builder resultsJson(String resultsJson) {
            this.resultsJson = resultsJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GateEvaluation build() {
            return new GateEvaluation(this);
        }
    }
}
