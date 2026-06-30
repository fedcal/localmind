export interface Document {
  id: string;
  filename: string;
  filePath: string;
  mimeType: string;
  fileSize: number;
  status: 'PENDING' | 'PROCESSING' | 'INDEXED' | 'FAILED' | 'ARCHIVED';
  createdAt: string;
  indexedAt?: string;
  ocrConfidence?: number;
}
