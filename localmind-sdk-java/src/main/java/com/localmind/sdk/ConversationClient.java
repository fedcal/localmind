package com.localmind.sdk;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Client for conversation management operations.
 *
 * <pre>{@code
 * ConversationClient convos = client.conversations();
 * List<ConversationSummary> all = convos.list();
 * Conversation full = convos.getById("abc-123");
 * byte[] exported = convos.export("abc-123", "pdf");
 * convos.delete("abc-123");
 * }</pre>
 */
public class ConversationClient {

    private final LocalMindClient client;

    ConversationClient(LocalMindClient client) {
        this.client = client;
    }

    /**
     * List all conversations.
     */
    public List<ConversationSummary> list() {
        String json = client.get("/conversations");
        return client.parseJson(json, new TypeReference<>() {});
    }

    /**
     * List conversations filtered by tag.
     */
    public List<ConversationSummary> listByTag(String tag) {
        String json = client.get("/conversations?tag=" + urlEncode(tag));
        return client.parseJson(json, new TypeReference<>() {});
    }

    /**
     * Get a conversation with full message history.
     */
    public Conversation getById(String id) {
        String json = client.get("/conversations/" + id);
        return client.parseJson(json, Conversation.class);
    }

    /**
     * Rename a conversation.
     */
    public Conversation rename(String id, String newTitle) {
        String json = client.patch("/conversations/" + id, Map.of("title", newTitle));
        return client.parseJson(json, Conversation.class);
    }

    /**
     * Update the system prompt for a conversation.
     */
    public Conversation updateSystemPrompt(String id, String systemPrompt) {
        String json = client.patch("/conversations/" + id + "/system-prompt",
                Map.of("systemPrompt", systemPrompt));
        return client.parseJson(json, Conversation.class);
    }

    /**
     * Add a tag to a conversation.
     */
    public Conversation addTag(String id, String tag) {
        String json = client.post("/conversations/" + id + "/tags", Map.of("tag", tag));
        return client.parseJson(json, Conversation.class);
    }

    /**
     * Remove a tag from a conversation.
     */
    public Conversation removeTag(String id, String tag) {
        String json = client.delete("/conversations/" + id + "/tags/" + urlEncode(tag));
        return client.parseJson(json, Conversation.class);
    }

    /**
     * Export a conversation in the specified format.
     *
     * @param id     conversation ID
     * @param format one of "json", "md", "markdown", "pdf"
     * @return raw bytes of the exported file
     */
    public byte[] export(String id, String format) {
        return client.getBytes("/conversations/" + id + "/export?format=" + format);
    }

    /**
     * Export a conversation as JSON (default format).
     */
    public byte[] export(String id) {
        return export(id, "json");
    }

    /**
     * Delete a conversation.
     */
    public void delete(String id) {
        client.delete("/conversations/" + id);
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
