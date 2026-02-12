package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a generated test skeleton.
 * Used by the test-generator tool group (generateUnitTests, findEdgeCases, analyzeCoverage).
 */
public class GeneratedTest {

    private final String id;
    private final String sourceFilePath;
    private final String language;
    private final String framework;
    private final int functionsFound;
    private final String testCode;
    private final Instant createdAt;

    private GeneratedTest(Builder builder) {
        this.id = builder.id;
        this.sourceFilePath = builder.sourceFilePath;
        this.language = builder.language;
        this.framework = builder.framework;
        this.functionsFound = builder.functionsFound;
        this.testCode = builder.testCode;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public String getLanguage() {
        return language;
    }

    public String getFramework() {
        return framework;
    }

    public int getFunctionsFound() {
        return functionsFound;
    }

    public String getTestCode() {
        return testCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String sourceFilePath;
        private String language;
        private String framework;
        private int functionsFound;
        private String testCode;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder sourceFilePath(String sourceFilePath) {
            this.sourceFilePath = sourceFilePath;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder framework(String framework) {
            this.framework = framework;
            return this;
        }

        public Builder functionsFound(int functionsFound) {
            this.functionsFound = functionsFound;
            return this;
        }

        public Builder testCode(String testCode) {
            this.testCode = testCode;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GeneratedTest build() {
            return new GeneratedTest(this);
        }
    }
}
