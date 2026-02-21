import { DocumentClient } from './documents';
import { ConversationClient } from './conversations';
import { LocalMindException } from './errors';
import type {
  LocalMindClientOptions,
  ChatOptions,
  ChatResponse,
  SearchResult,
  HealthStatus,
  AuthResponse,
} from './types';

/**
 * Main entry point for the LocalMind JavaScript/TypeScript SDK.
 *
 * @example
 * ```typescript
 * import { LocalMindClient } from '@localmind/sdk';
 *
 * const client = new LocalMindClient();
 * const response = await client.chat("Riassumi il documento...");
 * console.log(response.content);
 * ```
 */
export class LocalMindClient {
  private readonly baseUrl: string;
  private authToken?: string;
  private readonly timeoutMs: number;

  private _documents?: DocumentClient;
  private _conversations?: ConversationClient;

  constructor(options: LocalMindClientOptions = {}) {
    this.baseUrl = (options.baseUrl || 'http://localhost:8080/api/v1').replace(/\/+$/, '');
    this.authToken = options.authToken;
    this.timeoutMs = options.timeoutMs ?? 120_000;
  }

  // ---- Authentication ----

  /**
   * Authenticate with the LocalMind instance using a password.
   * The token is stored internally and used for subsequent requests.
   *
   * @returns The JWT token string.
   */
  async login(password: string): Promise<string> {
    const data = await this._post<AuthResponse>('/auth/login', { password });
    this.authToken = data.token;
    return data.token;
  }

  // ---- Chat ----

  /**
   * Send a chat message to LocalMind.
   *
   * @param message - The user message.
   * @param options - Optional chat configuration.
   */
  async chat(message: string, options?: ChatOptions): Promise<ChatResponse> {
    const body: Record<string, unknown> = { message };
    if (options) {
      if (options.provider) body.provider = options.provider;
      if (options.model) body.model = options.model;
      if (options.conversationId) body.conversationId = options.conversationId;
      if (options.systemPrompt) body.systemPrompt = options.systemPrompt;
      if (options.temperature !== undefined) body.temperature = options.temperature;
      if (options.maxTokens) body.maxTokens = options.maxTokens;
      body.enableRag = options.enableRag ?? false;
      body.enableToolCalling = options.enableToolCalling ?? false;
    } else {
      body.enableRag = false;
      body.enableToolCalling = false;
    }
    return this._post<ChatResponse>('/chat', body);
  }

  // ---- Search ----

  /**
   * Semantic search across indexed documents.
   *
   * @param query - Natural language search query.
   * @param topK - Maximum number of results (default: 5).
   */
  async search(query: string, topK: number = 5): Promise<SearchResult[]> {
    return this._post<SearchResult[]>('/documents/search', { query, topK });
  }

  // ---- Health ----

  /**
   * Check the health status of the LocalMind platform.
   */
  async health(): Promise<HealthStatus> {
    return this._get<HealthStatus>('/dashboard/health');
  }

  // ---- Sub-clients ----

  /** Access document management operations. */
  get documents(): DocumentClient {
    if (!this._documents) {
      this._documents = new DocumentClient(this);
    }
    return this._documents;
  }

  /** Access conversation management operations. */
  get conversations(): ConversationClient {
    if (!this._conversations) {
      this._conversations = new ConversationClient(this);
    }
    return this._conversations;
  }

  // ---- Internal HTTP methods ----

  /** @internal */
  async _get<T>(path: string): Promise<T> {
    const response = await this._fetch(path, { method: 'GET' });
    return response.json() as Promise<T>;
  }

  /** @internal */
  async _getBytes(path: string): Promise<ArrayBuffer> {
    const response = await this._fetch(path, { method: 'GET' });
    return response.arrayBuffer();
  }

  /** @internal */
  async _post<T>(path: string, body: unknown): Promise<T> {
    const response = await this._fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (response.status === 204) return undefined as unknown as T;
    return response.json() as Promise<T>;
  }

  /** @internal */
  async _postRaw<T>(path: string, body: Buffer | ArrayBuffer, headers: Record<string, string>): Promise<T> {
    const response = await this._fetch(path, {
      method: 'POST',
      headers,
      body,
    });
    return response.json() as Promise<T>;
  }

  /** @internal */
  async _postFormData<T>(path: string, formData: FormData): Promise<T> {
    const response = await this._fetch(path, {
      method: 'POST',
      body: formData as unknown as BodyInit,
    });
    return response.json() as Promise<T>;
  }

  /** @internal */
  async _patch<T>(path: string, body: unknown): Promise<T> {
    const response = await this._fetch(path, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (response.status === 204) return undefined as unknown as T;
    return response.json() as Promise<T>;
  }

  /** @internal */
  async _delete(path: string): Promise<void> {
    await this._fetch(path, { method: 'DELETE' });
  }

  /** @internal */
  async _deleteWithResponse<T>(path: string): Promise<T> {
    const response = await this._fetch(path, { method: 'DELETE' });
    if (response.status === 204) return undefined as unknown as T;
    return response.json() as Promise<T>;
  }

  private async _fetch(path: string, init: RequestInit): Promise<Response> {
    const url = `${this.baseUrl}${path}`;
    const headers: Record<string, string> = {
      ...(init.headers as Record<string, string> || {}),
    };
    if (this.authToken) {
      headers['Authorization'] = `Bearer ${this.authToken}`;
    }

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      const response = await fetch(url, {
        ...init,
        headers,
        signal: controller.signal,
      });

      if (!response.ok) {
        const body = await response.text().catch(() => '');
        throw new LocalMindException(
          `HTTP ${response.status} ${init.method} ${url}`,
          response.status,
          body,
        );
      }

      return response;
    } catch (error) {
      if (error instanceof LocalMindException) throw error;
      if (error instanceof Error && error.name === 'AbortError') {
        throw new LocalMindException(`Request timeout after ${this.timeoutMs}ms: ${init.method} ${url}`);
      }
      throw new LocalMindException(
        `Connection error: ${error instanceof Error ? error.message : String(error)}`
      );
    } finally {
      clearTimeout(timeout);
    }
  }
}
