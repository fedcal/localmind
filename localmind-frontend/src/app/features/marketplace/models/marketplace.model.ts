export interface MarketplaceAgent {
  id: string;
  name: string;
  description: string;
  author: string;
  version: string;
  category: string;
  downloadUrl: string;
  downloadCount: number;
  avgRating: number;
  publishedAt: string;
  createdAt: string;
}

export interface MarketplaceReview {
  id: string;
  agentId: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export type AgentCategory =
  | 'CHAT'
  | 'DOCUMENT_PROCESSING'
  | 'CODE_ASSISTANT'
  | 'DATA_ANALYSIS'
  | 'AUTOMATION'
  | 'INTEGRATION'
  | 'CUSTOM';

export const AGENT_CATEGORIES: AgentCategory[] = [
  'CHAT',
  'DOCUMENT_PROCESSING',
  'CODE_ASSISTANT',
  'DATA_ANALYSIS',
  'AUTOMATION',
  'INTEGRATION',
  'CUSTOM'
];
