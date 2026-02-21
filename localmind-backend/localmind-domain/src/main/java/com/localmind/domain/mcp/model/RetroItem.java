package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a single item within a retrospective.
 * Each item belongs to a category (e.g., mad, sad, glad) and can receive votes.
 */
public class RetroItem {

    private final String id;
    private final String retroId;
    private final String category;
    private final String content;
    private final int votes;
    private final Instant createdAt;

    private RetroItem(Builder builder) {
        this.id = builder.id;
        this.retroId = builder.retroId;
        this.category = builder.category;
        this.content = builder.content;
        this.votes = builder.votes;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRetroId() {
        return retroId;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public int getVotes() {
        return votes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String retroId;
        private String category;
        private String content;
        private int votes;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder retroId(String retroId) {
            this.retroId = retroId;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder votes(int votes) {
            this.votes = votes;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RetroItem build() {
            return new RetroItem(this);
        }
    }
}
