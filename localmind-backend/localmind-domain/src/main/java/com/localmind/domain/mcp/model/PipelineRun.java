package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a CI/CD pipeline run.
 * Used by the cicd-monitor tool group (listPipelines, getPipelineStatus, savePipelineRun, detectFlakyTests).
 */
public class PipelineRun {

    private final String id;
    private final String owner;
    private final String repo;
    private final String runId;
    private final String title;
    private final String branch;
    private final String status;
    private final String conclusion;
    private final String workflow;
    private final String url;
    private final Instant createdAt;

    private PipelineRun(Builder builder) {
        this.id = builder.id;
        this.owner = builder.owner;
        this.repo = builder.repo;
        this.runId = builder.runId;
        this.title = builder.title;
        this.branch = builder.branch;
        this.status = builder.status;
        this.conclusion = builder.conclusion;
        this.workflow = builder.workflow;
        this.url = builder.url;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getRunId() {
        return runId;
    }

    public String getTitle() {
        return title;
    }

    public String getBranch() {
        return branch;
    }

    public String getStatus() {
        return status;
    }

    public String getConclusion() {
        return conclusion;
    }

    public String getWorkflow() {
        return workflow;
    }

    public String getUrl() {
        return url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String owner;
        private String repo;
        private String runId;
        private String title;
        private String branch;
        private String status;
        private String conclusion;
        private String workflow;
        private String url;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public Builder runId(String runId) {
            this.runId = runId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder conclusion(String conclusion) {
            this.conclusion = conclusion;
            return this;
        }

        public Builder workflow(String workflow) {
            this.workflow = workflow;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PipelineRun build() {
            return new PipelineRun(this);
        }
    }
}
