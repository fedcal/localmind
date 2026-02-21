package com.localmind.sdk;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Full conversation including all messages.
 */
public class Conversation {

    private String id;
    private String title;
    private String systemPrompt;
    private Integer maxContextMessages;
    private Set<String> tags;
    private Map<String, Object> metadata;
    private List<ChatMessage> messages;
    private String createdAt;
    private String updatedAt;

    public Conversation() {
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

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Integer getMaxContextMessages() {
        return maxContextMessages;
    }

    public void setMaxContextMessages(Integer maxContextMessages) {
        this.maxContextMessages = maxContextMessages;
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

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
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
        return "Conversation{id='" + id + "'"
                + ", title='" + title + "'"
                + ", messages=" + (messages != null ? messages.size() : 0) + "}";
    }
}
