package com.localmind.sdk;

import java.util.List;

/**
 * Response from a chat request to the LocalMind API.
 */
public class ChatResponse {

    private String content;
    private String model;
    private String provider;
    private String conversationId;
    private TokenUsage tokenUsage;
    private long latencyMs;
    private List<RagSource> ragSources;

    public ChatResponse() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(TokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public List<RagSource> getRagSources() {
        return ragSources;
    }

    public void setRagSources(List<RagSource> ragSources) {
        this.ragSources = ragSources;
    }

    @Override
    public String toString() {
        return "ChatResponse{content='" + (content != null && content.length() > 100
                ? content.substring(0, 100) + "..." : content) + "'"
                + ", model='" + model + "'"
                + ", provider='" + provider + "'"
                + ", conversationId='" + conversationId + "'"
                + ", latencyMs=" + latencyMs + "}";
    }
}
