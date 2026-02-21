import * as fs from 'fs';
import * as path from 'path';

const BASE_URL = 'http://localhost:8080/api/v1';

export class ApiHelper {
  async healthCheck(): Promise<{ status: string }> {
    const res = await fetch(`${BASE_URL}/dashboard/health`);
    return res.json();
  }

  async listDocuments(): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/documents`);
    return res.json();
  }

  async deleteDocument(id: string): Promise<void> {
    await fetch(`${BASE_URL}/documents/${id}`, { method: 'DELETE' });
  }

  async uploadDocument(filePath: string, fileName: string): Promise<any> {
    const fileBuffer = fs.readFileSync(filePath);
    const blob = new Blob([fileBuffer], { type: 'text/plain' });
    const formData = new FormData();
    formData.append('file', blob, fileName);
    const res = await fetch(`${BASE_URL}/documents/upload`, {
      method: 'POST',
      body: formData,
    });
    return res.json();
  }

  async listProviders(): Promise<any[]> {
    const res = await fetch(`${BASE_URL}/settings/providers`);
    return res.json();
  }

  async deleteProvider(id: string): Promise<void> {
    await fetch(`${BASE_URL}/settings/providers/${id}`, { method: 'DELETE' });
  }

  async waitForDocumentStatus(
    docId: string,
    targetStatus: string,
    timeoutMs: number,
    pollIntervalMs = 2000,
  ): Promise<boolean> {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      const docs = await this.listDocuments();
      const doc = docs.find((d: any) => d.id === docId);
      if (doc && doc.status === targetStatus) return true;
      await new Promise((r) => setTimeout(r, pollIntervalMs));
    }
    return false;
  }

  async cleanupDocumentsByName(filenamePattern: string): Promise<void> {
    const docs = await this.listDocuments();
    for (const doc of docs) {
      if (doc.filename?.includes(filenamePattern)) {
        await this.deleteDocument(doc.id);
      }
    }
  }

  async cleanupProvidersByName(namePattern: string): Promise<void> {
    const providers = await this.listProviders();
    for (const p of providers) {
      if (p.name?.includes(namePattern)) {
        await this.deleteProvider(p.id);
      }
    }
  }
}
