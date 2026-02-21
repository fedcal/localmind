import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';

export interface SSEEvent {
  type: 'conversation' | 'token' | 'metadata' | 'done' | 'error';
  data: any;
}

export interface StreamChatRequest {
  message: string;
  provider?: string;
  model?: string;
  conversationId?: string | null;
  systemPrompt?: string | null;
  temperature?: number;
  maxTokens?: number;
  enableRag?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ChatStreamService {

  private abortController: AbortController | null = null;

  async streamChat(
    request: StreamChatRequest,
    onEvent: (event: SSEEvent) => void
  ): Promise<void> {
    this.abort();
    this.abortController = new AbortController();

    const url = `${environment.apiUrl}/chat/stream`;

    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
      signal: this.abortController.signal
    });

    if (!response.ok) {
      throw new Error(`Stream request failed: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('No response body');
    }

    const decoder = new TextDecoder();
    let buffer = '';

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        let currentEventType = '';
        let currentData = '';

        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEventType = line.substring(6).trim();
          } else if (line.startsWith('data:')) {
            currentData = line.substring(5).trim();
          } else if (line === '' && currentEventType && currentData) {
            try {
              const parsed = JSON.parse(currentData);
              onEvent({ type: currentEventType as SSEEvent['type'], data: parsed });
            } catch {
              onEvent({ type: currentEventType as SSEEvent['type'], data: currentData });
            }
            currentEventType = '';
            currentData = '';
          }
        }
      }
    } finally {
      reader.releaseLock();
      this.abortController = null;
    }
  }

  abort(): void {
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
  }
}
