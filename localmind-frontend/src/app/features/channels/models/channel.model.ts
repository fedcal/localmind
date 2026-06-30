export type MessagingPlatform = 'SLACK' | 'DISCORD' | 'TELEGRAM';

export interface MessagingChannel {
  id: string;
  name: string;
  platform: MessagingPlatform;
  botToken: string;
  webhookSecret: string;
  active: boolean;
  defaultSystemPrompt: string;
  defaultModel: string;
  defaultProvider: string;
  enableRag: boolean;
  enableToolCalling: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateChannelRequest {
  name: string;
  platform: MessagingPlatform;
  botToken: string;
  webhookSecret?: string;
  defaultSystemPrompt?: string;
  defaultModel?: string;
  defaultProvider?: string;
  enableRag: boolean;
  enableToolCalling: boolean;
}
