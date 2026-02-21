"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.DocumentClient = void 0;
const fs_1 = require("fs");
const path_1 = require("path");
/**
 * Client for document management operations.
 *
 * @example
 * ```typescript
 * const docs = client.documents;
 * const all = await docs.list();
 * const uploaded = await docs.upload("/path/to/report.pdf");
 * await docs.delete(uploaded.id);
 * ```
 */
class DocumentClient {
    constructor(client) {
        this.client = client;
    }
    /**
     * List all documents.
     */
    async list() {
        return this.client._get('/documents');
    }
    /**
     * Get a document by ID.
     */
    async get(id) {
        return this.client._get(`/documents/${id}`);
    }
    /**
     * Upload a file to LocalMind for indexing (Node.js only).
     *
     * @param filePath - Absolute or relative path to the file.
     */
    async upload(filePath) {
        const fileName = (0, path_1.basename)(filePath);
        const fileBuffer = (0, fs_1.readFileSync)(filePath);
        const boundary = `----LocalMindBoundary${Date.now()}`;
        const header = `--${boundary}\r\nContent-Disposition: form-data; name="file"; filename="${fileName}"\r\nContent-Type: application/octet-stream\r\n\r\n`;
        const footer = `\r\n--${boundary}--\r\n`;
        const headerBuffer = Buffer.from(header, 'utf-8');
        const footerBuffer = Buffer.from(footer, 'utf-8');
        const body = Buffer.concat([headerBuffer, fileBuffer, footerBuffer]);
        return this.client._postRaw('/documents/upload', body, {
            'Content-Type': `multipart/form-data; boundary=${boundary}`,
        });
    }
    /**
     * Upload a file using a Blob or File object (browser compatible).
     *
     * @param file - File or Blob object.
     * @param filename - File name.
     */
    async uploadBlob(file, filename) {
        const formData = new FormData();
        formData.append('file', file, filename);
        return this.client._postFormData('/documents/upload', formData);
    }
    /**
     * Delete a document by ID.
     */
    async delete(id) {
        await this.client._delete(`/documents/${id}`);
    }
}
exports.DocumentClient = DocumentClient;
//# sourceMappingURL=documents.js.map