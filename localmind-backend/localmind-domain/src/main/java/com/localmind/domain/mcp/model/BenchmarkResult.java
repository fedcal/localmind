package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing the result of a performance profiler operation.
 * Used by the performance-profiler tool group (analyzeBundle, findBottlenecks, benchmarkCompare).
 */
public class BenchmarkResult {

    private final String id;
    private final String name;
    private final String language;
    private final String resultJson;
    private final Instant createdAt;

    private BenchmarkResult(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.language = builder.language;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
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
        private String name;
        private String language;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
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

        public BenchmarkResult build() {
            return new BenchmarkResult(this);
        }
    }
}
