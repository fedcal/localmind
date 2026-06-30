/**
 * Options for creating a LocalMindClient instance.
 */
export interface LocalMindClientOptions {
  /** Base URL of the LocalMind API (default: http://localhost:8080/api/v1). */
  baseUrl?: string;
  /** Pre-set JWT authentication token. */
  authToken?: string;
  /** Request timeout in milliseconds (default: 120000). */
  timeoutMs?: number;
}

/**
 * Options for a chat request.
 */
export interface ChatOptions {
  /** LLM provider override: OLLAMA, OPENAI, ANTHROPIC, GOOGLE. */
  provider?: string;
  /** Model name override (e.g. "llama3", "gpt-4o", "claude-sonnet-4-20250514"). */
  model?: string;
  /** Existing conversation ID to continue. */
  conversationId?: string;
  /** System prompt for the conversation. */
  systemPrompt?: string;
  /** Generation temperature (0.0-2.0, default 0.7). */
  temperature?: number;
  /** Maximum tokens to generate. */
  maxTokens?: number;
  /** Enable RAG from indexed documents. */
  enableRag?: boolean;
  /** Enable MCP tool calling. */
  enableToolCalling?: boolean;
}

/**
 * Token usage statistics from an LLM response.
 */
export interface TokenUsage {
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
}

/**
 * A RAG source document chunk returned alongside a chat response.
 */
export interface RagSource {
  documentId: string;
  filename: string;
  chunkIndex: number;
  score: number;
  contentPreview: string;
}

/**
 * Response from a chat request.
 */
export interface ChatResponse {
  content: string;
  model: string;
  provider: string;
  conversationId: string;
  tokenUsage: TokenUsage | null;
  latencyMs: number;
  ragSources: RagSource[] | null;
}

/**
 * A document stored in LocalMind.
 */
export interface Document {
  id: string;
  filename: string;
  filePath: string;
  mimeType: string;
  fileSize: number;
  status: string;
  createdAt: string;
  indexedAt: string | null;
  ocrConfidence: number | null;
}

/**
 * A semantic search result.
 */
export interface SearchResult {
  documentId: string;
  filename: string;
  content: string;
  score: number;
  chunkIndex: number;
}

/**
 * A single message within a conversation.
 */
export interface ChatMessage {
  role: string;
  content: string;
  metadata: Record<string, unknown> | null;
}

/**
 * Summary of a conversation (without full message history).
 */
export interface ConversationSummary {
  id: string;
  title: string;
  messageCount: number;
  tags: string[];
  metadata: Record<string, unknown> | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Full conversation including all messages.
 */
export interface Conversation {
  id: string;
  title: string;
  systemPrompt: string | null;
  maxContextMessages: number | null;
  tags: string[];
  metadata: Record<string, unknown> | null;
  messages: ChatMessage[];
  createdAt: string;
  updatedAt: string;
}

/**
 * Health status of the LocalMind platform.
 */
export interface HealthStatus {
  status: string;
  services: Record<string, string>;
}

/**
 * Authentication response.
 */
export interface AuthResponse {
  token: string;
  expiresIn: number;
}
