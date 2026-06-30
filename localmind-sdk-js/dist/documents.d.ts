import type { LocalMindClient } from './client';
import type { Document } from './types';
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
export declare class DocumentClient {
    private readonly client;
    constructor(client: LocalMindClient);
    /**
     * List all documents.
     */
    list(): Promise<Document[]>;
    /**
     * Get a document by ID.
     */
    get(id: string): Promise<Document>;
    /**
     * Upload a file to LocalMind for indexing (Node.js only).
     *
     * @param filePath - Absolute or relative path to the file.
     */
    upload(filePath: string): Promise<Document>;
    /**
     * Upload a file using a Blob or File object (browser compatible).
     *
     * @param file - File or Blob object.
     * @param filename - File name.
     */
    uploadBlob(file: Blob, filename: string): Promise<Document>;
    /**
     * Delete a document by ID.
     */
    delete(id: string): Promise<void>;
}
//# sourceMappingURL=documents.d.ts.map