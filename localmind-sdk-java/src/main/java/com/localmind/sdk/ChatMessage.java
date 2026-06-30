package com.localmind.sdk;

import java.util.Map;

/**
 * A single message within a conversation.
 */
public class ChatMessage {

    private String role;
    private String content;
    private Map<String, Object> metadata;

    public ChatMessage() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ChatMessage{role='" + role + "'"
                + ", content='" + (content != null && content.length() > 80
                ? content.substring(0, 80) + "..." : content) + "'}";
    }
}
