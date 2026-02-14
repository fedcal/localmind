export interface ChatMessage {
  role: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL';
  content: string;
  metadata?: Record<string, unknown>;
}

export interface ChatRequest {
  message: string;
  provider?: string;
  model?: string;
  conversationId?: string;
  systemPrompt?: string;
  temperature?: number;
  maxTokens?: number;
  enableToolCalling?: boolean;
  enableRag?: boolean;
}

export interface ChatResponse {
  content: string;
  model: string;
  provider: string;
  conversationId?: string;
  tokenUsage?: TokenUsage;
  latencyMs: number;
  ragSources?: RagSource[];
}

export interface RagSource {
  documentId: string;
  filename: string;
  chunkIndex: number;
  score: number;
  contentPreview: string;
}

export interface TokenUsage {
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
}

export interface Conversation {
  id: string;
  title: string;
  messages: ChatMessage[];
  createdAt: string;
}

export interface ConversationSummary {
  id: string;
  title: string;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  tags?: string[];
  metadata?: Record<string, unknown>;
}

export interface ConversationDetail {
  id: string;
  title: string;
  systemPrompt?: string;
  maxContextMessages?: number;
  messages: ChatMessage[];
  createdAt: string;
  updatedAt: string;
  tags?: string[];
  metadata?: Record<string, unknown>;
}

export interface UpdateSystemPromptRequest {
  systemPrompt: string | null;
}

export interface RenameRequest {
  title: string;
}

export interface AddToolResultRequest {
  toolName: string;
  content: string;
  metadata?: Record<string, unknown>;
}

export interface AddTagRequest {
  tag: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  hasMore: boolean;
}
