export type ProviderType = 'OLLAMA' | 'OPENAI' | 'ANTHROPIC' | 'GOOGLE';

export interface LlmProviderConfig {
  id: string;
  name: string;
  type: ProviderType;
  baseUrl: string;
  apiKey?: string;
  enabled: boolean;
  models: string[];
  defaultModel?: string;
}

export interface CreateProviderRequest {
  name: string;
  type: ProviderType;
  baseUrl: string;
  apiKey?: string;
  defaultModel?: string;
}

export interface OllamaStatus {
  online: boolean;
  version?: string;
  models: OllamaModelDetail[];
  errorMessage?: string;
}

export interface OllamaModelDetail {
  name: string;
  size: string;
  modifiedAt: string;
}
