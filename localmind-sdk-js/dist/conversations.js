"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ConversationClient = void 0;
/**
 * Client for conversation management operations.
 *
 * @example
 * ```typescript
 * const convos = client.conversations;
 * const all = await convos.list();
 * const full = await convos.getById("abc-123");
 * const pdf = await convos.export("abc-123", "pdf");
 * await convos.delete("abc-123");
 * ```
 */
class ConversationClient {
    constructor(client) {
        this.client = client;
    }
    /**
     * List all conversations, optionally filtered by tag.
     */
    async list(tag) {
        const params = tag ? `?tag=${encodeURIComponent(tag)}` : '';
        return this.client._get(`/conversations${params}`);
    }
    /**
     * Get a conversation with full message history.
     */
    async getById(id) {
        return this.client._get(`/conversations/${id}`);
    }
    /**
     * Rename a conversation.
     */
    async rename(id, title) {
        return this.client._patch(`/conversations/${id}`, { title });
    }
    /**
     * Update the system prompt for a conversation.
     */
    async updateSystemPrompt(id, systemPrompt) {
        return this.client._patch(`/conversations/${id}/system-prompt`, { systemPrompt });
    }
    /**
     * Add a tag to a conversation.
     */
    async addTag(id, tag) {
        return this.client._post(`/conversations/${id}/tags`, { tag });
    }
    /**
     * Remove a tag from a conversation.
     */
    async removeTag(id, tag) {
        return this.client._deleteWithResponse(`/conversations/${id}/tags/${encodeURIComponent(tag)}`);
    }
    /**
     * Export a conversation in the specified format.
     *
     * @param id - Conversation UUID.
     * @param format - Export format: "json", "md", "markdown", or "pdf".
     * @returns Raw bytes of the exported file as an ArrayBuffer.
     */
    async export(id, format = 'json') {
        return this.client._getBytes(`/conversations/${id}/export?format=${format}`);
    }
    /**
     * Delete a conversation.
     */
    async delete(id) {
        await this.client._delete(`/conversations/${id}`);
    }
}
exports.ConversationClient = ConversationClient;
//# sourceMappingURL=conversations.js.map