# State Management with Angular Signals

**Project:** LocalMind
**Version:** 1.0.0
**Date:** 2026-02-18
**Framework:** Angular 21.0.0

---

## Table of Contents

1. [Overview](#1-overview)
2. [Signal Primitives](#2-signal-primitives)
3. [State Pattern in LocalMind](#3-state-pattern-in-localmind)
4. [ChatStore: Complete Example](#4-chatstore-complete-example)
5. [ThemeService: Second Example of Signal-Based Store](#5-themeservice-second-example-of-signal-based-store)
6. [Usage in Components](#6-usage-in-components)
7. [Computed Signals for Derived State](#7-computed-signals-for-derived-state)
8. [Effects for Side Effects](#8-effects-for-side-effects)
9. [Comparison with NgRx](#9-comparison-with-ngrx)
10. [Guidelines](#10-guidelines)

---

## 1. Overview

Angular Signals is a reactive primitive introduced in Angular 16 and became mature and stable in Angular 21. In LocalMind, Signals is the only mechanism for application state management. No external state management libraries are used (NgRx, Akita, NGXS, Elf).

### Design principles

- **Simplicity**: state is managed through standard Angular services with internal Signals.
- **Encapsulation**: writable Signals are private; components only access readonly Signals.
- **Zero external dependencies**: no additional libraries required.
- **Type safety**: the TypeScript type system ensures consistency of reactive data.
- **Fine granularity**: each signal tracks a single piece of state, enabling targeted DOM updates.

---

## 2. Signal Primitives

### signal()

Creates a mutable reactive value. It is the fundamental building block.

```typescript
import { signal } from '@angular/core';

// Creation
const count = signal(0);

// Reading (invocation as a function)
console.log(count());  // 0

// Writing
count.set(5);          // Sets the value
count.update(v => v + 1);  // Updates based on the current value
```

### computed()

Creates a derived value that automatically updates when dependencies change.

```typescript
import { signal, computed } from '@angular/core';

const items = signal<string[]>(['a', 'b', 'c']);
const itemCount = computed(() => items().length);  // Automatically 3
```

- **Lazy**: the value is computed only on first read.
- **Cached**: the value is recomputed only when a dependency changes.
- **Readonly**: it is not possible to assign a value directly.

### effect()

Executes a side effect when one or more dependent Signals change.

```typescript
import { signal, effect } from '@angular/core';

const message = signal('Hello');

effect(() => {
  console.log('Message changed:', message());
});
```

- **Automatic tracking**: Angular automatically detects which Signals are read within the effect.
- **Asynchronous execution**: effects are executed asynchronously after rendering.
- **Cleanup**: the framework automatically handles effect cleanup upon component/service destruction.

### .asReadonly()

Converts a WritableSignal into a read-only Signal.

```typescript
const _count = signal(0);
const count = _count.asReadonly();  // Signal<number> (not WritableSignal)

count();      // OK: read
// count.set(5)  // COMPILE ERROR: 'set' does not exist on Signal<number>
```

---

## 3. State Pattern in LocalMind

### Store architecture

```
+-----------------------------------------------------------+
|  Injectable Store Service (providedIn: 'root')            |
|                                                           |
|  Private (writable):                                      |
|    _messages = signal<ChatMessage[]>([])                  |
|    _isLoading = signal(false)                             |
|    _selectedProvider = signal<string>('OLLAMA')           |
|    _isStreaming = signal(false)                           |
|    _advancedMode = signal(false)                          |
|                                                           |
|  Public (readonly):                                       |
|    messages = this._messages.asReadonly()                 |
|    isLoading = this._isLoading.asReadonly()               |
|    selectedProvider = this._selectedProvider.asReadonly() |
|    isStreaming = this._isStreaming.asReadonly()            |
|    advancedMode = this._advancedMode.asReadonly()         |
|                                                           |
|  Mutation methods:                                        |
|    addMessage(msg: ChatMessage): void                     |
|    setLoading(value: boolean): void                       |
|    clearMessages(): void                                  |
|    setProvider(provider: string): void                    |
|    setStreaming(value: boolean): void                     |
|    toggleAdvancedMode(): void                             |
+-----------------------------------------------------------+
          ^
          | inject()
          |
+--------------------------------------------------+
|  Component                                       |
|                                                  |
|  store = inject(ChatStore);                      |
|  messages = this.store.messages;                 |
|  isLoading = this.store.isLoading;               |
|                                                  |
|  Template:                                       |
|  @for (msg of messages(); track msg.id) { ... }  |
|  @if (isLoading()) { <spinner /> }               |
+--------------------------------------------------+
```

### Pattern rules

1. **Writable signals are always private**: `_` prefix and `private` visibility.
2. **Readonly signals are public**: exposed via `.asReadonly()`.
3. **Mutations happen only through methods**: components never access writable signals directly.
4. **Store as singleton service**: `@Injectable({ providedIn: 'root' })`.
5. **Injection via `inject()`**: components inject the store using the `inject()` function.

---

## 4. ChatStore: Complete Example

### Implementation

```typescript
import { Injectable, signal, computed } from '@angular/core';
import { ChatMessage, ConversationSummary } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatStore {

  // === Private signals (writable) ===

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
  private _isStreaming = signal(false);
  private _advancedMode = signal<boolean>(
    localStorage.getItem('localmind-chat-mode') === 'advanced'
  );
  private _filterTag = signal<string | null>(null);
  private _searchQuery = signal('');
  private _currentPage = signal(0);
  private _totalPages = signal(0);
  private _totalElements = signal(0);
  private _hasMore = signal(false);

  // === Public signals (readonly) ===

  readonly messages = this._messages.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly selectedProvider = this._selectedProvider.asReadonly();
  readonly selectedModel = this._selectedModel.asReadonly();
  readonly error = this._error.asReadonly();
  readonly currentConversationId = this._currentConversationId.asReadonly();
  readonly currentConversationTitle = this._currentConversationTitle.asReadonly();
  readonly conversations = this._conversations.asReadonly();
  readonly conversationsLoading = this._conversationsLoading.asReadonly();
  readonly currentSystemPrompt = this._currentSystemPrompt.asReadonly();
  readonly toolCallingEnabled = this._toolCallingEnabled.asReadonly();
  readonly ragEnabled = this._ragEnabled.asReadonly();
  readonly maxContextMessages = this._maxContextMessages.asReadonly();
  readonly isStreaming = this._isStreaming.asReadonly();
  readonly advancedMode = this._advancedMode.asReadonly();
  readonly filterTag = this._filterTag.asReadonly();
  readonly searchQuery = this._searchQuery.asReadonly();
  readonly currentPage = this._currentPage.asReadonly();
  readonly totalPages = this._totalPages.asReadonly();
  readonly totalElements = this._totalElements.asReadonly();
  readonly hasMore = this._hasMore.asReadonly();

  // === Computed signals (derived state) ===

  readonly messageCount = computed(() => this._messages().length);
  readonly hasMessages = computed(() => this._messages().length > 0);
  readonly lastMessage = computed(() => {
    const msgs = this._messages();
    return msgs.length > 0 ? msgs[msgs.length - 1] : null;
  });

  // === Mutation methods ===

  addMessage(message: ChatMessage): void {
    this._messages.update(msgs => [...msgs, message]);
  }

  setLoading(value: boolean): void {
    this._isLoading.set(value);
  }

  clearMessages(): void {
    this._messages.set([]);
  }

  setProvider(provider: string): void {
    this._selectedProvider.set(provider);
  }

  setModel(model: string): void {
    this._selectedModel.set(model);
  }

  setError(error: string | null): void {
    this._error.set(error);
  }

  addUserMessage(content: string): void {
    this.addMessage({
      id: crypto.randomUUID(),
      role: 'USER',
      content,
      createdAt: new Date().toISOString()
    });
  }

  addAssistantMessage(content: string): void {
    this.addMessage({
      id: crypto.randomUUID(),
      role: 'ASSISTANT',
      content,
      createdAt: new Date().toISOString()
    });
  }

  // --- Streaming ---

  setStreaming(value: boolean): void {
    this._isStreaming.set(value);
  }

  addStreamingAssistantMessage(): void {
    this.addMessage({
      id: crypto.randomUUID(),
      role: 'ASSISTANT',
      content: '',
      createdAt: new Date().toISOString()
    });
  }

  appendTokenToLastMessage(token: string): void {
    this._messages.update(msgs => {
      const updated = [...msgs];
      const last = updated[updated.length - 1];
      if (last && last.role === 'ASSISTANT') {
        updated[updated.length - 1] = { ...last, content: last.content + token };
      }
      return updated;
    });
  }

  // --- Conversations ---

  setCurrentConversation(id: string | null, title: string | null): void {
    this._currentConversationId.set(id);
    this._currentConversationTitle.set(title);
  }

  updateConversationTitle(title: string): void {
    this._currentConversationTitle.set(title);
  }

  loadConversationMessages(messages: ChatMessage[]): void {
    this._messages.set(messages);
  }

  removeConversation(id: string): void {
    this._conversations.update(convs => convs.filter(c => c.id !== id));
  }

  // --- Pagination ---

  setPaginationInfo(page: number, totalPages: number, totalElements: number, hasMore: boolean): void {
    this._currentPage.set(page);
    this._totalPages.set(totalPages);
    this._totalElements.set(totalElements);
    this._hasMore.set(hasMore);
  }

  appendConversations(conversations: ConversationSummary[]): void {
    this._conversations.update(existing => [...existing, ...conversations]);
  }

  resetPagination(): void {
    this._currentPage.set(0);
    this._totalPages.set(0);
    this._totalElements.set(0);
    this._hasMore.set(false);
    this._conversations.set([]);
  }

  // --- Advanced Mode ---

  toggleAdvancedMode(): void {
    this._advancedMode.update(v => {
      const newValue = !v;
      localStorage.setItem('localmind-chat-mode', newValue ? 'advanced' : 'simple');
      return newValue;
    });
  }

  // --- Tool Calling and RAG ---

  toggleToolCalling(): void {
    this._toolCallingEnabled.update(v => !v);
  }

  toggleRag(): void {
    this._ragEnabled.update(v => !v);
  }

  setSystemPrompt(prompt: string | null): void {
    this._currentSystemPrompt.set(prompt);
  }

  setMaxContextMessages(max: number | null): void {
    this._maxContextMessages.set(max);
  }
}
```

### Store signals

#### Private writable signals

| Signal                      | Type                                        | Description                                      |
|-----------------------------|---------------------------------------------|--------------------------------------------------|
| `_messages`                 | `WritableSignal<ChatMessage[]>`             | Conversation message list                        |
| `_isLoading`                | `WritableSignal<boolean>`                   | Loading flag                                     |
| `_selectedProvider`         | `WritableSignal<string>`                    | Selected LLM provider                            |
| `_selectedModel`            | `WritableSignal<string>`                    | Selected LLM model                               |
| `_error`                    | `WritableSignal<string\|null>`              | Current error message                            |
| `_currentConversationId`    | `WritableSignal<string\|null>`              | Current conversation ID                          |
| `_currentConversationTitle` | `WritableSignal<string\|null>`              | Current conversation title                       |
| `_conversations`            | `WritableSignal<ConversationSummary[]>`     | Conversation list                                |
| `_conversationsLoading`     | `WritableSignal<boolean>`                   | Conversations loading flag                       |
| `_currentSystemPrompt`      | `WritableSignal<string\|null>`              | Current system prompt                            |
| `_toolCallingEnabled`       | `WritableSignal<boolean>`                   | Tool calling enabled                             |
| `_ragEnabled`               | `WritableSignal<boolean>`                   | RAG enabled                                      |
| `_maxContextMessages`       | `WritableSignal<number\|null>`              | Context message limit                            |
| `_isStreaming`              | `WritableSignal<boolean>`                   | SSE streaming in progress                        |
| `_advancedMode`             | `WritableSignal<boolean>`                   | Advanced mode (persisted in localStorage)        |
| `_filterTag`                | `WritableSignal<string\|null>`              | Conversation filter tag                          |
| `_searchQuery`              | `WritableSignal<string>`                    | Conversation search query                        |
| `_currentPage`              | `WritableSignal<number>`                    | Current pagination page                          |
| `_totalPages`               | `WritableSignal<number>`                    | Total pages                                      |
| `_totalElements`            | `WritableSignal<number>`                    | Total elements                                   |
| `_hasMore`                  | `WritableSignal<boolean>`                   | Flag for additional pages                        |

#### Public readonly signals

| Signal                     | Type                              | Description                                      |
|----------------------------|-----------------------------------|--------------------------------------------------|
| `messages`                 | `Signal<ChatMessage[]>`           | Conversation message list                        |
| `isLoading`                | `Signal<boolean>`                 | Loading state                                    |
| `selectedProvider`         | `Signal<string>`                  | Selected LLM provider                            |
| `selectedModel`            | `Signal<string>`                  | Selected LLM model                               |
| `error`                    | `Signal<string\|null>`            | Current error message                            |
| `currentConversationId`    | `Signal<string\|null>`            | Current conversation ID                          |
| `currentConversationTitle` | `Signal<string\|null>`            | Current conversation title                       |
| `conversations`            | `Signal<ConversationSummary[]>`   | Conversation list                                |
| `conversationsLoading`     | `Signal<boolean>`                 | Conversations loading flag                       |
| `currentSystemPrompt`      | `Signal<string\|null>`            | Current system prompt                            |
| `toolCallingEnabled`       | `Signal<boolean>`                 | Tool calling enabled                             |
| `ragEnabled`               | `Signal<boolean>`                 | RAG enabled                                      |
| `maxContextMessages`       | `Signal<number\|null>`            | Context message limit                            |
| `isStreaming`              | `Signal<boolean>`                 | SSE streaming in progress                        |
| `advancedMode`             | `Signal<boolean>`                 | Advanced mode                                    |
| `filterTag`                | `Signal<string\|null>`            | Conversation filter tag                          |
| `searchQuery`              | `Signal<string>`                  | Conversation search query                        |
| `currentPage`              | `Signal<number>`                  | Current pagination page                          |
| `totalPages`               | `Signal<number>`                  | Total pages                                      |
| `totalElements`            | `Signal<number>`                  | Total elements                                   |
| `hasMore`                  | `Signal<boolean>`                 | Flag for additional pages                        |

#### Computed signals

| Signal         | Type                            | Description                            |
|----------------|---------------------------------|----------------------------------------|
| `messageCount` | `Signal<number>`                | Number of messages in the conversation |
| `hasMessages`  | `Signal<boolean>`               | Whether messages are present           |
| `lastMessage`  | `Signal<ChatMessage \| null>`   | Last message in the conversation       |

### Mutation methods

| Method                       | Parameters                                             | Description                                              |
|------------------------------|--------------------------------------------------------|----------------------------------------------------------|
| `addMessage`                 | `message: ChatMessage`                                 | Adds a message to the list                               |
| `setLoading`                 | `value: boolean`                                       | Sets the loading state                                   |
| `clearMessages`              | -                                                      | Clears the message list                                  |
| `setProvider`                | `provider: string`                                     | Sets the LLM provider                                    |
| `setModel`                   | `model: string`                                        | Sets the LLM model                                       |
| `setError`                   | `error: string \| null`                                | Sets the error message                                   |
| `addUserMessage`             | `content: string`                                      | Adds a user message with auto-generated UUID             |
| `addAssistantMessage`        | `content: string`                                      | Adds an assistant message with auto-generated UUID       |
| `setStreaming`               | `value: boolean`                                       | Sets the SSE streaming state                             |
| `addStreamingAssistantMessage` | -                                                    | Adds an empty assistant message for streaming            |
| `appendTokenToLastMessage`   | `token: string`                                        | Appends a token to the last assistant message            |
| `setCurrentConversation`     | `id: string \| null, title: string \| null`            | Sets the current conversation                            |
| `updateConversationTitle`    | `title: string`                                        | Updates the current conversation title                   |
| `loadConversationMessages`   | `messages: ChatMessage[]`                              | Loads messages for a conversation                        |
| `removeConversation`         | `id: string`                                           | Removes a conversation from the list                     |
| `setPaginationInfo`          | `page, totalPages, totalElements, hasMore`             | Sets pagination information                              |
| `appendConversations`        | `conversations: ConversationSummary[]`                 | Appends conversations to the existing list               |
| `resetPagination`            | -                                                      | Resets pagination and clears the conversation list        |
| `toggleAdvancedMode`         | -                                                      | Toggles advanced mode (persists to localStorage)         |
| `toggleToolCalling`          | -                                                      | Toggles tool calling enablement                          |
| `toggleRag`                  | -                                                      | Toggles RAG enablement                                   |
| `setSystemPrompt`            | `prompt: string \| null`                               | Sets the system prompt                                   |
| `setMaxContextMessages`      | `max: number \| null`                                  | Sets the context message limit                           |

---

## 5. ThemeService: Second Example of Signal-Based Store

### Overview

The ThemeService manages the application theme (light/dark) via Signals, following the same pattern as the ChatStore. It represents a minimal store example combining signals, computed, effect, and localStorage persistence.

### Implementation

```typescript
import { Injectable, signal, computed, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private _currentTheme = signal<'light' | 'dark'>(this.getInitialTheme());
  readonly currentTheme = this._currentTheme.asReadonly();
  readonly isDark = computed(() => this._currentTheme() === 'dark');

  constructor() {
    effect(() => {
      document.documentElement.setAttribute('data-theme', this._currentTheme());
      localStorage.setItem('localmind-theme', this._currentTheme());
    });
  }

  toggle(): void {
    this._currentTheme.update(t => t === 'light' ? 'dark' : 'light');
  }

  private getInitialTheme(): 'light' | 'dark' {
    return localStorage.getItem('localmind-theme') as 'light' | 'dark'
      ?? (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  }
}
```

### ThemeService signals

| Signal         | Type                              | Visibility | Description                        |
|----------------|-----------------------------------|------------|------------------------------------|
| `_currentTheme`| `WritableSignal<'light'\|'dark'>` | Private    | Current theme                      |
| `currentTheme` | `Signal<'light'\|'dark'>`         | Public     | Readonly: current theme            |
| `isDark`       | `Signal<boolean>`                 | Public     | Computed: whether theme is dark    |

### How it works

1. **Initialization**: the initial theme is read from localStorage. If not present, it uses the operating system preference via `matchMedia('(prefers-color-scheme: dark)')`.
2. **Reactive effect**: every time the `_currentTheme` signal changes, the effect updates the `data-theme` attribute on `<html>` and persists the choice to localStorage.
3. **Toggle**: the `toggle()` method alternates between `'light'` and `'dark'`, automatically triggering the effect.

This pattern applies to any global state that requires persistence and side effects: language preferences, sidebar layout, font size, etc.

---

## 6. Usage in Components

### ChatPageComponent

```typescript
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatStore } from '../state/chat.store';
import { ChatService } from '../services/chat.service';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.scss'
})
export class ChatPageComponent {

  private chatStore = inject(ChatStore);
  private chatService = inject(ChatService);

  // Signals exposed to the template
  messages = this.chatStore.messages;
  isLoading = this.chatStore.isLoading;
  selectedProvider = this.chatStore.selectedProvider;
  selectedModel = this.chatStore.selectedModel;

  // Component local state
  userInput = '';

  sendMessage(): void {
    if (!this.userInput.trim() || this.chatStore.isLoading()) return;

    const message = this.userInput.trim();
    this.userInput = '';

    // Adds the user message to the store
    this.chatStore.addUserMessage(message);
    this.chatStore.setLoading(true);

    // API call
    this.chatService.chat({
      message,
      provider: this.chatStore.selectedProvider(),
      model: this.chatStore.selectedModel()
    }).subscribe({
      next: (response) => {
        this.chatStore.addAssistantMessage(response.content);
        this.chatStore.setLoading(false);
      },
      error: () => {
        this.chatStore.setLoading(false);
      }
    });
  }
}
```

### Template (chat-page.component.html)

```html
<div class="chat-container">
  <div class="messages">
    @for (msg of messages(); track msg.id) {
      <div class="message" [class]="msg.role.toLowerCase()">
        <div class="message-content">{{ msg.content }}</div>
      </div>
    }
    @if (isLoading()) {
      <div class="message assistant loading">
        <div class="typing-indicator">...</div>
      </div>
    }
  </div>

  <div class="input-area">
    <input
      type="text"
      [(ngModel)]="userInput"
      (keyup.enter)="sendMessage()"
      placeholder="Scrivi un messaggio..."
      [disabled]="isLoading()"
    />
    <button (click)="sendMessage()" [disabled]="isLoading()">
      Invia
    </button>
  </div>
</div>
```

### Key points

- **`messages()`**: signal invocation in the template to get the current value.
- **`@for ... track msg.id`**: new Angular iteration syntax with tracking for optimized DOM updates.
- **`@if (isLoading())`**: new Angular conditional syntax.
- **`[(ngModel)]`**: two-way binding for user input (local state, not in the store).
- **`[disabled]="isLoading()"`**: reactive binding based on the loading signal.

---

## 7. Computed Signals for Derived State

`computed()` signals derive state from existing signals without duplication:

```typescript
// Message count
readonly messageCount = computed(() => this._messages().length);

// Messages present
readonly hasMessages = computed(() => this._messages().length > 0);

// Last message
readonly lastMessage = computed(() => {
  const msgs = this._messages();
  return msgs.length > 0 ? msgs[msgs.length - 1] : null;
});

// Example in the template
@if (hasMessages()) {
  <span>{{ messageCount() }} messaggi</span>
}
```

### Computed properties

- **Lazy**: computation happens only when the value is read.
- **Cached**: the value is recomputed only if a dependency changes.
- **Composable**: a computed can depend on other computeds.
- **Glitch-free**: Angular guarantees consistency of derived values.

---

## 8. Effects for Side Effects

`effect()` is used for operations that must happen in response to state changes:

```typescript
import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ChatStore {

  private _selectedProvider = signal<string>('OLLAMA');

  constructor() {
    // Side effect: logs the provider change
    effect(() => {
      console.log('Provider cambiato:', this._selectedProvider());
    });

    // Side effect: saves the preference to localStorage
    effect(() => {
      localStorage.setItem('selectedProvider', this._selectedProvider());
    });
  }
}
```

### Effect use cases in LocalMind

| Use case                      | Description                                         |
|-------------------------------|-----------------------------------------------------|
| localStorage persistence      | Saving user preferences (provider, model, theme, chat mode) |
| Logging                       | Logging state changes for debugging                 |
| DOM synchronization           | Updating HTML attributes (e.g., `data-theme`)       |
| Synchronization               | Triggering API calls in response to changes         |

---

## 9. Comparison with NgRx

| Aspect                    | Angular Signals (LocalMind)       | NgRx                                                                   |
|---------------------------|-----------------------------------|------------------------------------------------------------------------|
| **Setup**                 | Zero configuration                | Actions, Reducers, Effects, Selectors, Store module                    |
| **Boilerplate**           | Minimal (signal + method)         | High (4+ files per feature)                                           |
| **npm dependencies**      | None                              | `@ngrx/store`, `@ngrx/effects`, `@ngrx/entity`, `@ngrx/store-devtools` |
| **Type safety**           | Native TypeScript                 | Requires generics and configuration                                    |
| **Learning curve**        | 1-2 hours                         | 1-2 days                                                              |
| **DevTools**              | Angular DevTools (Signals tab)    | Redux DevTools (time-travel debugging)                                 |
| **Immutability**          | Convention (`update` with spread) | Enforced by the framework                                              |
| **Scalability**           | Optimal for small-to-medium apps  | Necessary for very complex enterprise apps                             |
| **Testing**               | Standard Angular testing          | Requires NgRx-specific testing                                        |

### When to prefer NgRx

NgRx becomes advantageous when:
- State is shared across many disconnected components in the tree.
- Time-travel debugging is needed.
- State operations are complex and composed.
- The team already has experience with the Redux pattern.

For LocalMind v1.0.0, Angular Signals is the appropriate choice given the moderate complexity of the application state. The ChatStore demonstrates that even with 20+ signals, the pattern remains manageable and readable.

---

## 10. Guidelines

### When to use each primitive

| Primitive   | When to use                                                 |
|-------------|-------------------------------------------------------------|
| `signal()`  | Mutable state that needs to be tracked and updated          |
| `computed()`| Derived state that depends on other signals                 |
| `effect()`  | Side effects (localStorage, logging, DOM sync, reactive API calls) |

### Naming conventions

| Pattern                     | Example                                        |
|-----------------------------|------------------------------------------------|
| Private writable signal     | `_messages`, `_isLoading`, `_isStreaming`       |
| Public readonly signal      | `messages`, `isLoading`, `isStreaming`          |
| Computed signal             | `messageCount`, `hasMessages`, `isDark`         |
| Mutation method             | `addMessage()`, `setLoading()`, `toggle()`     |

### Best practices

1. **Never expose writable signals**: always use `.asReadonly()`.
2. **Mutations through methods**: components should not call `.set()` or `.update()` directly.
3. **Computed for derived state**: never duplicate state; use `computed()`.
4. **Minimal effects**: one effect per side effect; avoid complex effects.
5. **Singleton store**: `providedIn: 'root'` to share state across components.
6. **Local state in the component**: for UI-only state (e.g., `userInput`), use normal component properties.
7. **Persistence via effect**: for state that must survive refresh (theme, chat mode), use `effect()` with `localStorage`.
8. **Initialization from localStorage**: read the initial value in the signal constructor to restore state on load.
