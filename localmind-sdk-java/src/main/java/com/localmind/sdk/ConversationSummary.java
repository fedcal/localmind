package com.localmind.sdk;

import java.util.Map;
import java.util.Set;

/**
 * Summary of a conversation (without full message history).
 */
public class ConversationSummary {

    private String id;
    private String title;
    private int messageCount;
    private Set<String> tags;
    private Map<String, Object> metadata;
    private String createdAt;
    private String updatedAt;

    public ConversationSummary() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ConversationSummary{id='" + id + "'"
                + ", title='" + title + "'"
                + ", messageCount=" + messageCount + "}";
    }
}
