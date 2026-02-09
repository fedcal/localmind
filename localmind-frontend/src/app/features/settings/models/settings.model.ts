export interface LlmProviderConfig {
  id: string;
  name: string;
  type: 'OLLAMA' | 'OPENAI' | 'ANTHROPIC';
  baseUrl: string;
  apiKey?: string;
  enabled: boolean;
  models: string[];
  defaultModel?: string;
}

export interface CreateProviderRequest {
  name: string;
  type: 'OLLAMA' | 'OPENAI' | 'ANTHROPIC';
  baseUrl: string;
  apiKey?: string;
  defaultModel?: string;
}
