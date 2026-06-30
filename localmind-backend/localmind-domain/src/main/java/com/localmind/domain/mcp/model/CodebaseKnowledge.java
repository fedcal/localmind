package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a codebase knowledge analysis result.
 */
public class CodebaseKnowledge {

    private final String id;
    private final String analysisType;
    private final String targetPath;
    private final String resultJson;
    private final Instant createdAt;

    private CodebaseKnowledge(Builder builder) {
        this.id = builder.id;
        this.analysisType = builder.analysisType;
        this.targetPath = builder.targetPath;
        this.resultJson = builder.resultJson;
        this.createdAt = builder.createdAt;
    }

    public String getId() { return id; }
    public String getAnalysisType() { return analysisType; }
    public String getTargetPath() { return targetPath; }
    public String getResultJson() { return resultJson; }
    public Instant getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String analysisType;
        private String targetPath;
        private String resultJson;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder analysisType(String analysisType) { this.analysisType = analysisType; return this; }
        public Builder targetPath(String targetPath) { this.targetPath = targetPath; return this; }
        public Builder resultJson(String resultJson) { this.resultJson = resultJson; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public CodebaseKnowledge build() { return new CodebaseKnowledge(this); }
    }
}
