import type { LocalMindClient } from './client';
import type { Conversation, ConversationSummary } from './types';
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
export declare class ConversationClient {
    private readonly client;
    constructor(client: LocalMindClient);
    /**
     * List all conversations, optionally filtered by tag.
     */
    list(tag?: string): Promise<ConversationSummary[]>;
    /**
     * Get a conversation with full message history.
     */
    getById(id: string): Promise<Conversation>;
    /**
     * Rename a conversation.
     */
    rename(id: string, title: string): Promise<Conversation>;
    /**
     * Update the system prompt for a conversation.
     */
    updateSystemPrompt(id: string, systemPrompt: string): Promise<Conversation>;
    /**
     * Add a tag to a conversation.
     */
    addTag(id: string, tag: string): Promise<Conversation>;
    /**
     * Remove a tag from a conversation.
     */
    removeTag(id: string, tag: string): Promise<Conversation>;
    /**
     * Export a conversation in the specified format.
     *
     * @param id - Conversation UUID.
     * @param format - Export format: "json", "md", "markdown", or "pdf".
     * @returns Raw bytes of the exported file as an ArrayBuffer.
     */
    export(id: string, format?: string): Promise<ArrayBuffer>;
    /**
     * Delete a conversation.
     */
    delete(id: string): Promise<void>;
}
//# sourceMappingURL=conversations.d.ts.map