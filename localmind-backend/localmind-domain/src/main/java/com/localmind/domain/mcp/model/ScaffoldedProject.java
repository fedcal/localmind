package com.localmind.domain.mcp.model;

import java.time.Instant;

public class ScaffoldedProject {

    private final String id;
    private final String templateName;
    private final String projectName;
    private final String outputPath;
    private final int filesGenerated;
    private final Instant createdAt;

    private ScaffoldedProject(Builder builder) {
        this.id = builder.id;
        this.templateName = builder.templateName;
        this.projectName = builder.projectName;
        this.outputPath = builder.outputPath;
        this.filesGenerated = builder.filesGenerated;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public int getFilesGenerated() {
        return filesGenerated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String templateName;
        private String projectName;
        private String outputPath;
        private int filesGenerated;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder outputPath(String outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        public Builder filesGenerated(int filesGenerated) {
            this.filesGenerated = filesGenerated;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ScaffoldedProject build() {
            return new ScaffoldedProject(this);
        }
    }
}
