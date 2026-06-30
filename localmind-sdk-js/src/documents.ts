import type { LocalMindClient } from './client';
import type { Document } from './types';
import { readFileSync } from 'fs';
import { basename } from 'path';

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
export class DocumentClient {
  constructor(private readonly client: LocalMindClient) {}

  /**
   * List all documents.
   */
  async list(): Promise<Document[]> {
    return this.client._get<Document[]>('/documents');
  }

  /**
   * Get a document by ID.
   */
  async get(id: string): Promise<Document> {
    return this.client._get<Document>(`/documents/${id}`);
  }

  /**
   * Upload a file to LocalMind for indexing (Node.js only).
   *
   * @param filePath - Absolute or relative path to the file.
   */
  async upload(filePath: string): Promise<Document> {
    const fileName = basename(filePath);
    const fileBuffer = readFileSync(filePath);

    const boundary = `----LocalMindBoundary${Date.now()}`;
    const header = `--${boundary}\r\nContent-Disposition: form-data; name="file"; filename="${fileName}"\r\nContent-Type: application/octet-stream\r\n\r\n`;
    const footer = `\r\n--${boundary}--\r\n`;

    const headerBuffer = Buffer.from(header, 'utf-8');
    const footerBuffer = Buffer.from(footer, 'utf-8');
    const body = Buffer.concat([headerBuffer, fileBuffer, footerBuffer]);

    return this.client._postRaw<Document>('/documents/upload', body, {
      'Content-Type': `multipart/form-data; boundary=${boundary}`,
    });
  }

  /**
   * Upload a file using a Blob or File object (browser compatible).
   *
   * @param file - File or Blob object.
   * @param filename - File name.
   */
  async uploadBlob(file: Blob, filename: string): Promise<Document> {
    const formData = new FormData();
    formData.append('file', file, filename);
    return this.client._postFormData<Document>('/documents/upload', formData);
  }

  /**
   * Delete a document by ID.
   */
  async delete(id: string): Promise<void> {
    await this.client._delete(`/documents/${id}`);
  }
}
