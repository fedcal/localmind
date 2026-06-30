package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing the result of a log analysis.
 * Used by the log-analyzer tool group (analyzeLogFile, findErrorPatterns, tailLog, generateLogSummary).
 */
public class LogAnalysis {

    private final String id;
    private final int totalLines;
    private final String levelsJson;
    private final String topErrorsJson;
    private final Instant createdAt;

    private LogAnalysis(Builder builder) {
        this.id = builder.id;
        this.totalLines = builder.totalLines;
        this.levelsJson = builder.levelsJson;
        this.topErrorsJson = builder.topErrorsJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public String getLevelsJson() {
        return levelsJson;
    }

    public String getTopErrorsJson() {
        return topErrorsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private int totalLines;
        private String levelsJson;
        private String topErrorsJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder totalLines(int totalLines) {
            this.totalLines = totalLines;
            return this;
        }

        public Builder levelsJson(String levelsJson) {
            this.levelsJson = levelsJson;
            return this;
        }

        public Builder topErrorsJson(String topErrorsJson) {
            this.topErrorsJson = topErrorsJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LogAnalysis build() {
            return new LogAnalysis(this);
        }
    }
}
