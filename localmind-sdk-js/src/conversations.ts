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
export class ConversationClient {
  constructor(private readonly client: LocalMindClient) {}

  /**
   * List all conversations, optionally filtered by tag.
   */
  async list(tag?: string): Promise<ConversationSummary[]> {
    const params = tag ? `?tag=${encodeURIComponent(tag)}` : '';
    return this.client._get<ConversationSummary[]>(`/conversations${params}`);
  }

  /**
   * Get a conversation with full message history.
   */
  async getById(id: string): Promise<Conversation> {
    return this.client._get<Conversation>(`/conversations/${id}`);
  }

  /**
   * Rename a conversation.
   */
  async rename(id: string, title: string): Promise<Conversation> {
    return this.client._patch<Conversation>(`/conversations/${id}`, { title });
  }

  /**
   * Update the system prompt for a conversation.
   */
  async updateSystemPrompt(id: string, systemPrompt: string): Promise<Conversation> {
    return this.client._patch<Conversation>(`/conversations/${id}/system-prompt`, { systemPrompt });
  }

  /**
   * Add a tag to a conversation.
   */
  async addTag(id: string, tag: string): Promise<Conversation> {
    return this.client._post<Conversation>(`/conversations/${id}/tags`, { tag });
  }

  /**
   * Remove a tag from a conversation.
   */
  async removeTag(id: string, tag: string): Promise<Conversation> {
    return this.client._deleteWithResponse<Conversation>(`/conversations/${id}/tags/${encodeURIComponent(tag)}`);
  }

  /**
   * Export a conversation in the specified format.
   *
   * @param id - Conversation UUID.
   * @param format - Export format: "json", "md", "markdown", or "pdf".
   * @returns Raw bytes of the exported file as an ArrayBuffer.
   */
  async export(id: string, format: string = 'json'): Promise<ArrayBuffer> {
    return this.client._getBytes(`/conversations/${id}/export?format=${format}`);
  }

  /**
   * Delete a conversation.
   */
  async delete(id: string): Promise<void> {
    await this.client._delete(`/conversations/${id}`);
  }
}
