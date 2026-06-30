package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a generated mock dataset.
 * Used by the data-mock-generator tool group (generateMockData, generateMockJson, generateMockCsv).
 */
public class GeneratedDataset {

    private final String id;
    private final String name;
    private final String format;
    private final int rowCount;
    private final String schemaJson;
    private final String sampleDataJson;
    private final Instant createdAt;

    private GeneratedDataset(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.format = builder.format;
        this.rowCount = builder.rowCount;
        this.schemaJson = builder.schemaJson;
        this.sampleDataJson = builder.sampleDataJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public int getRowCount() {
        return rowCount;
    }

    public String getSchemaJson() {
        return schemaJson;
    }

    public String getSampleDataJson() {
        return sampleDataJson;
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
        private String format;
        private int rowCount;
        private String schemaJson;
        private String sampleDataJson;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder rowCount(int rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder schemaJson(String schemaJson) {
            this.schemaJson = schemaJson;
            return this;
        }

        public Builder sampleDataJson(String sampleDataJson) {
            this.sampleDataJson = sampleDataJson;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GeneratedDataset build() {
            return new GeneratedDataset(this);
        }
    }
}
