"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LocalMindClient = void 0;
const documents_1 = require("./documents");
const conversations_1 = require("./conversations");
const errors_1 = require("./errors");
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
class LocalMindClient {
    constructor(options = {}) {
        this.baseUrl = (options.baseUrl || 'http://localhost:8080/api/v1').replace(/\/+$/, '');
        this.authToken = options.authToken;
        this.timeoutMs = options.timeoutMs ?? 120000;
    }
    // ---- Authentication ----
    /**
     * Authenticate with the LocalMind instance using a password.
     * The token is stored internally and used for subsequent requests.
     *
     * @returns The JWT token string.
     */
    async login(password) {
        const data = await this._post('/auth/login', { password });
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
    async chat(message, options) {
        const body = { message };
        if (options) {
            if (options.provider)
                body.provider = options.provider;
            if (options.model)
                body.model = options.model;
            if (options.conversationId)
                body.conversationId = options.conversationId;
            if (options.systemPrompt)
                body.systemPrompt = options.systemPrompt;
            if (options.temperature !== undefined)
                body.temperature = options.temperature;
            if (options.maxTokens)
                body.maxTokens = options.maxTokens;
            body.enableRag = options.enableRag ?? false;
            body.enableToolCalling = options.enableToolCalling ?? false;
        }
        else {
            body.enableRag = false;
            body.enableToolCalling = false;
        }
        return this._post('/chat', body);
    }
    // ---- Search ----
    /**
     * Semantic search across indexed documents.
     *
     * @param query - Natural language search query.
     * @param topK - Maximum number of results (default: 5).
     */
    async search(query, topK = 5) {
        return this._post('/documents/search', { query, topK });
    }
    // ---- Health ----
    /**
     * Check the health status of the LocalMind platform.
     */
    async health() {
        return this._get('/dashboard/health');
    }
    // ---- Sub-clients ----
    /** Access document management operations. */
    get documents() {
        if (!this._documents) {
            this._documents = new documents_1.DocumentClient(this);
        }
        return this._documents;
    }
    /** Access conversation management operations. */
    get conversations() {
        if (!this._conversations) {
            this._conversations = new conversations_1.ConversationClient(this);
        }
        return this._conversations;
    }
    // ---- Internal HTTP methods ----
    /** @internal */
    async _get(path) {
        const response = await this._fetch(path, { method: 'GET' });
        return response.json();
    }
    /** @internal */
    async _getBytes(path) {
        const response = await this._fetch(path, { method: 'GET' });
        return response.arrayBuffer();
    }
    /** @internal */
    async _post(path, body) {
        const response = await this._fetch(path, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });
        if (response.status === 204)
            return undefined;
        return response.json();
    }
    /** @internal */
    async _postRaw(path, body, headers) {
        const response = await this._fetch(path, {
            method: 'POST',
            headers,
            body,
        });
        return response.json();
    }
    /** @internal */
    async _postFormData(path, formData) {
        const response = await this._fetch(path, {
            method: 'POST',
            body: formData,
        });
        return response.json();
    }
    /** @internal */
    async _patch(path, body) {
        const response = await this._fetch(path, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });
        if (response.status === 204)
            return undefined;
        return response.json();
    }
    /** @internal */
    async _delete(path) {
        await this._fetch(path, { method: 'DELETE' });
    }
    /** @internal */
    async _deleteWithResponse(path) {
        const response = await this._fetch(path, { method: 'DELETE' });
        if (response.status === 204)
            return undefined;
        return response.json();
    }
    async _fetch(path, init) {
        const url = `${this.baseUrl}${path}`;
        const headers = {
            ...(init.headers || {}),
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
                throw new errors_1.LocalMindException(`HTTP ${response.status} ${init.method} ${url}`, response.status, body);
            }
            return response;
        }
        catch (error) {
            if (error instanceof errors_1.LocalMindException)
                throw error;
            if (error instanceof Error && error.name === 'AbortError') {
                throw new errors_1.LocalMindException(`Request timeout after ${this.timeoutMs}ms: ${init.method} ${url}`);
            }
            throw new errors_1.LocalMindException(`Connection error: ${error instanceof Error ? error.message : String(error)}`);
        }
        finally {
            clearTimeout(timeout);
        }
    }
}
exports.LocalMindClient = LocalMindClient;
//# sourceMappingURL=client.js.map