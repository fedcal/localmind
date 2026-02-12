package com.localmind.domain.mcp.model;

import java.time.Instant;
import java.util.Set;

public class CodeSnippet {

    private final String id;
    private final String title;
    private final String code;
    private final String language;
    private final String description;
    private final Set<String> tags;
    private final Instant createdAt;
    private final Instant updatedAt;

    private CodeSnippet(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.code = builder.code;
        this.language = builder.language;
        this.description = builder.description;
        this.tags = builder.tags;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private String code;
        private String language;
        private String description;
        private Set<String> tags;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CodeSnippet build() {
            return new CodeSnippet(this);
        }
    }
}
