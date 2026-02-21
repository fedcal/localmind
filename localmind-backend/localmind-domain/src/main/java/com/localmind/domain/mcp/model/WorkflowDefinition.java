package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a workflow definition.
 * Contains the workflow name, trigger event, steps, and activation status.
 */
public class WorkflowDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final String triggerEvent;
    private final String triggerConditionsJson;
    private final String stepsJson;
    private final boolean active;
    private final Instant createdAt;

    private WorkflowDefinition(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.triggerEvent = builder.triggerEvent;
        this.triggerConditionsJson = builder.triggerConditionsJson;
        this.stepsJson = builder.stepsJson;
        this.active = builder.active;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public String getTriggerConditionsJson() {
        return triggerConditionsJson;
    }

    public String getStepsJson() {
        return stepsJson;
    }

    public boolean isActive() {
        return active;
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
        private String description;
        private String triggerEvent;
        private String triggerConditionsJson;
        private String stepsJson;
        private boolean active;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder triggerEvent(String triggerEvent) {
            this.triggerEvent = triggerEvent;
            return this;
        }

        public Builder triggerConditionsJson(String triggerConditionsJson) {
            this.triggerConditionsJson = triggerConditionsJson;
            return this;
        }

        public Builder stepsJson(String stepsJson) {
            this.stepsJson = stepsJson;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WorkflowDefinition build() {
            return new WorkflowDefinition(this);
        }
    }
}
