package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing the result of a database schema exploration.
 * Used by the db-schema-explorer tool group (exploreSchema, describeTable, suggestIndexes, generateErd).
 */
public class SchemaExploration {

    private final String id;
    private final String jdbcUrl;
    private final int tableCount;
    private final String resultJson;
    private final Instant exploredAt;

    private SchemaExploration(Builder builder) {
        this.id = builder.id;
        this.jdbcUrl = builder.jdbcUrl;
        this.tableCount = builder.tableCount;
        this.resultJson = builder.resultJson;
        this.exploredAt = builder.exploredAt;
    }

    public String getId() {
        return id;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public int getTableCount() {
        return tableCount;
    }

    public String getResultJson() {
        return resultJson;
    }

    public Instant getExploredAt() {
        return exploredAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String jdbcUrl;
        private int tableCount;
        private String resultJson;
        private Instant exploredAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder tableCount(int tableCount) {
            this.tableCount = tableCount;
            return this;
        }

        public Builder resultJson(String resultJson) {
            this.resultJson = resultJson;
            return this;
        }

        public Builder exploredAt(Instant exploredAt) {
            this.exploredAt = exploredAt;
            return this;
        }

        public SchemaExploration build() {
            return new SchemaExploration(this);
        }
    }
}
