package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing an incident in the incident management system.
 * Tracks incidents from opening through investigation, mitigation, resolution and post-mortem.
 */
public class Incident {

    private final String id;
    private final String title;
    private final String severity;
    private final String status;
    private final String description;
    private final String affectedSystemsJson;
    private final String timelineJson;
    private final String resolution;
    private final String rootCause;
    private final Instant createdAt;
    private final Instant resolvedAt;

    private Incident(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.severity = builder.severity;
        this.status = builder.status;
        this.description = builder.description;
        this.affectedSystemsJson = builder.affectedSystemsJson;
        this.timelineJson = builder.timelineJson;
        this.resolution = builder.resolution;
        this.rootCause = builder.rootCause;
        this.createdAt = builder.createdAt;
        this.resolvedAt = builder.resolvedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getAffectedSystemsJson() {
        return affectedSystemsJson;
    }

    public String getTimelineJson() {
        return timelineJson;
    }

    public String getResolution() {
        return resolution;
    }

    public String getRootCause() {
        return rootCause;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private String severity;
        private String status;
        private String description;
        private String affectedSystemsJson;
        private String timelineJson;
        private String resolution;
        private String rootCause;
        private Instant createdAt;
        private Instant resolvedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder affectedSystemsJson(String affectedSystemsJson) {
            this.affectedSystemsJson = affectedSystemsJson;
            return this;
        }

        public Builder timelineJson(String timelineJson) {
            this.timelineJson = timelineJson;
            return this;
        }

        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder rootCause(String rootCause) {
            this.rootCause = rootCause;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder resolvedAt(Instant resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Incident build() {
            return new Incident(this);
        }
    }
}
