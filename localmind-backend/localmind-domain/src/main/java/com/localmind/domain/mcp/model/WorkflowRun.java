package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a single execution run of a workflow.
 * Tracks the run status, payload, step results and timing.
 */
public class WorkflowRun {

    private final String id;
    private final String workflowId;
    private final String status;
    private final String payloadJson;
    private final String stepResultsJson;
    private final Instant startedAt;
    private final Instant completedAt;

    private WorkflowRun(Builder builder) {
        this.id = builder.id;
        this.workflowId = builder.workflowId;
        this.status = builder.status;
        this.payloadJson = builder.payloadJson;
        this.stepResultsJson = builder.stepResultsJson;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getStatus() {
        return status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public String getStepResultsJson() {
        return stepResultsJson;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String workflowId;
        private String status;
        private String payloadJson;
        private String stepResultsJson;
        private Instant startedAt;
        private Instant completedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder workflowId(String workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder payloadJson(String payloadJson) {
            this.payloadJson = payloadJson;
            return this;
        }

        public Builder stepResultsJson(String stepResultsJson) {
            this.stepResultsJson = stepResultsJson;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public WorkflowRun build() {
            return new WorkflowRun(this);
        }
    }
}
