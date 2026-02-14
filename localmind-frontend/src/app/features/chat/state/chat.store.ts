import { Injectable, signal, computed } from '@angular/core';
import { ChatMessage, ConversationSummary } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatStore {
  private _messages = signal<ChatMessage[]>([]);
  private _isLoading = signal(false);
  private _selectedProvider = signal('OLLAMA');
  private _selectedModel = signal('llama3.2');
  private _error = signal<string | null>(null);

  private _currentConversationId = signal<string | null>(null);
  private _currentConversationTitle = signal<string | null>(null);
  private _conversations = signal<ConversationSummary[]>([]);
  private _conversationsLoading = signal(false);
  private _currentSystemPrompt = signal<string | null>(null);
  private _toolCallingEnabled = signal(false);
  private _ragEnabled = signal(false);
  private _maxContextMessages = signal<number | null>(null);
  private _filterTag = signal<string | null>(null);
  private _searchQuery = signal('');
  private _currentPage = signal(0);
  private _totalPages = signal(0);
  private _totalElements = signal(0);
  private _hasMore = signal(false);

  readonly messages = this._messages.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly selectedProvider = this._selectedProvider.asReadonly();
  readonly selectedModel = this._selectedModel.asReadonly();
  readonly error = this._error.asReadonly();

  readonly messageCount = computed(() => this._messages().length);

  readonly currentConversationId = this._currentConversationId.asReadonly();
  readonly currentConversationTitle = this._currentConversationTitle.asReadonly();
  readonly conversations = this._conversations.asReadonly();
  readonly conversationsLoading = this._conversationsLoading.asReadonly();
  readonly currentSystemPrompt = this._currentSystemPrompt.asReadonly();
  readonly toolCallingEnabled = computed(() => this._toolCallingEnabled());
  readonly ragEnabled = computed(() => this._ragEnabled());
  readonly maxContextMessages = this._maxContextMessages.asReadonly();
  readonly filterTag = this._filterTag.asReadonly();
  readonly searchQuery = this._searchQuery.asReadonly();
  readonly currentPage = this._currentPage.asReadonly();
  readonly totalPages = this._totalPages.asReadonly();
  readonly totalElements = this._totalElements.asReadonly();
  readonly hasMore = this._hasMore.asReadonly();
  readonly filteredConversations = computed(() => {
    const tag = this._filterTag();
    const convs = this._conversations();
    if (!tag) return convs;
    return convs.filter(c => c.tags?.includes(tag));
  });

  toggleToolCalling() {
    this._toolCallingEnabled.update(v => !v);
  }

  toggleRag() {
    this._ragEnabled.update(v => !v);
  }

  setMaxContextMessages(value: number | null) {
    this._maxContextMessages.set(value);
  }

  setFilterTag(tag: string | null) {
    this._filterTag.set(tag);
  }

  setSearchQuery(query: string) {
    this._searchQuery.set(query);
  }

  setPaginationInfo(page: number, totalPages: number, totalElements: number, hasMore: boolean) {
    this._currentPage.set(page);
    this._totalPages.set(totalPages);
    this._totalElements.set(totalElements);
    this._hasMore.set(hasMore);
  }

  appendConversations(conversations: ConversationSummary[]) {
    this._conversations.update(existing => [...existing, ...conversations]);
  }

  resetPagination() {
    this._currentPage.set(0);
    this._totalPages.set(0);
    this._totalElements.set(0);
    this._hasMore.set(false);
    this._searchQuery.set('');
  }

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

  setSystemPrompt(prompt: string | null) {
    this._currentSystemPrompt.set(prompt);
  }

  clearMessages() {
    this._messages.set([]);
    this._currentConversationId.set(null);
    this._currentConversationTitle.set(null);
    this._currentSystemPrompt.set(null);
    this._maxContextMessages.set(null);
    this._error.set(null);
  }

  setCurrentConversation(id: string | null, title: string | null) {
    this._currentConversationId.set(id);
    this._currentConversationTitle.set(title);
  }

  setConversations(conversations: ConversationSummary[]) {
    this._conversations.set(conversations);
  }

  setConversationsLoading(loading: boolean) {
    this._conversationsLoading.set(loading);
  }

  updateConversationTitle(id: string, title: string) {
    this._conversations.update(convs =>
      convs.map(c => c.id === id ? { ...c, title } : c)
    );
    if (this._currentConversationId() === id) {
      this._currentConversationTitle.set(title);
    }
  }

  removeConversation(id: string) {
    this._conversations.update(convs => convs.filter(c => c.id !== id));
    if (this._currentConversationId() === id) {
      this.clearMessages();
    }
  }

  loadConversationMessages(messages: ChatMessage[], id: string, title: string, systemPrompt?: string, maxContextMessages?: number) {
    this._messages.set(messages);
    this._currentConversationId.set(id);
    this._currentConversationTitle.set(title);
    this._currentSystemPrompt.set(systemPrompt ?? null);
    this._maxContextMessages.set(maxContextMessages ?? null);
    this._error.set(null);
  }
}
