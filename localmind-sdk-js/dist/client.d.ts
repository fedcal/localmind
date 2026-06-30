import { DocumentClient } from './documents';
import { ConversationClient } from './conversations';
import type { LocalMindClientOptions, ChatOptions, ChatResponse, SearchResult, HealthStatus } from './types';
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
export declare class LocalMindClient {
    private readonly baseUrl;
    private authToken?;
    private readonly timeoutMs;
    private _documents?;
    private _conversations?;
    constructor(options?: LocalMindClientOptions);
    /**
     * Authenticate with the LocalMind instance using a password.
     * The token is stored internally and used for subsequent requests.
     *
     * @returns The JWT token string.
     */
    login(password: string): Promise<string>;
    /**
     * Send a chat message to LocalMind.
     *
     * @param message - The user message.
     * @param options - Optional chat configuration.
     */
    chat(message: string, options?: ChatOptions): Promise<ChatResponse>;
    /**
     * Semantic search across indexed documents.
     *
     * @param query - Natural language search query.
     * @param topK - Maximum number of results (default: 5).
     */
    search(query: string, topK?: number): Promise<SearchResult[]>;
    /**
     * Check the health status of the LocalMind platform.
     */
    health(): Promise<HealthStatus>;
    /** Access document management operations. */
    get documents(): DocumentClient;
    /** Access conversation management operations. */
    get conversations(): ConversationClient;
    /** @internal */
    _get<T>(path: string): Promise<T>;
    /** @internal */
    _getBytes(path: string): Promise<ArrayBuffer>;
    /** @internal */
    _post<T>(path: string, body: unknown): Promise<T>;
    /** @internal */
    _postRaw<T>(path: string, body: Buffer | ArrayBuffer, headers: Record<string, string>): Promise<T>;
    /** @internal */
    _postFormData<T>(path: string, formData: FormData): Promise<T>;
    /** @internal */
    _patch<T>(path: string, body: unknown): Promise<T>;
    /** @internal */
    _delete(path: string): Promise<void>;
    /** @internal */
    _deleteWithResponse<T>(path: string): Promise<T>;
    private _fetch;
}
//# sourceMappingURL=client.d.ts.map