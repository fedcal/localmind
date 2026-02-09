import { Injectable, signal, computed } from '@angular/core';
import { ChatMessage } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatStore {
  private _messages = signal<ChatMessage[]>([]);
  private _isLoading = signal(false);
  private _selectedProvider = signal('OLLAMA');
  private _selectedModel = signal('llama3.2');
  private _error = signal<string | null>(null);

  readonly messages = this._messages.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly selectedProvider = this._selectedProvider.asReadonly();
  readonly selectedModel = this._selectedModel.asReadonly();
  readonly error = this._error.asReadonly();

  readonly messageCount = computed(() => this._messages().length);

  addMessage(message: ChatMessage) {
    this._messages.update(msgs => [...msgs, message]);
    this._error.set(null);
  }

  setLoading(loading: boolean) {
    this._isLoading.set(loading);
  }

  setProvider(provider: string) {
    this._selectedProvider.set(provider);
  }

  setModel(model: string) {
    this._selectedModel.set(model);
  }

  setError(error: string | null) {
    this._error.set(error);
  }

  clearMessages() {
    this._messages.set([]);
    this._error.set(null);
  }
}
