export interface SearchRequest {
  query: string;
  topK?: number;
}

export interface SearchResult {
  content: string;
  score: number;
  documentId: string;
  documentName: string;
  metadata?: Record<string, unknown>;
}
