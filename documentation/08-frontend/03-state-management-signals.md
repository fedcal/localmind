# State Management with Angular Signals

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Framework:** Angular 21.0.0

---

## Table of Contents

1. [Overview](#1-overview)
2. [Signal Primitives](#2-signal-primitives)
3. [State Pattern in LocalMind](#3-state-pattern-in-localmind)
4. [ChatStore: Complete Example](#4-chatstore-complete-example)
5. [Usage in Components](#5-usage-in-components)
6. [Computed Signals for Derived State](#6-computed-signals-for-derived-state)
7. [Effects for Side Effects](#7-effects-for-side-effects)
8. [Comparison with NgRx](#8-comparison-with-ngrx)
9. [Guidelines](#9-guidelines)

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
|                                                           |
|  Public (readonly):                                       |
|    messages = this._messages.asReadonly()                 |
|    isLoading = this._isLoading.asReadonly()               |
|    selectedProvider = this._selectedProvider.asReadonly() |
|                                                           |
|  Mutation methods:                                        |
|    addMessage(msg: ChatMessage): void                     |
|    setLoading(value: boolean): void                       |
|    clearMessages(): void                                  |
|    setProvider(provider: string): void                    |
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
import { ChatMessage } from '../models/chat-message.model';

@Injectable({ providedIn: 'root' })
export class ChatStore {

  // === Private signals (writable) ===

  private _messages = signal<ChatMessage[]>([]);
  private _isLoading = signal(false);
  private _selectedProvider = signal<string>('OLLAMA');
  private _selectedModel = signal<string>('llama3.2');

  // === Public signals (readonly) ===

  readonly messages = this._messages.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly selectedProvider = this._selectedProvider.asReadonly();
  readonly selectedModel = this._selectedModel.asReadonly();

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
}
```

### ChatMessage model

```typescript
export interface ChatMessage {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}
```

### Store signals

| Signal               | Type                            | Visibility | Description                            |
|----------------------|---------------------------------|------------|----------------------------------------|
| `_messages`          | `WritableSignal<ChatMessage[]>` | Private    | Conversation message list              |
| `_isLoading`         | `WritableSignal<boolean>`       | Private    | Loading flag                           |
| `_selectedProvider`  | `WritableSignal<string>`        | Private    | Selected LLM provider                  |
| `_selectedModel`     | `WritableSignal<string>`        | Private    | Selected LLM model                     |
| `messages`           | `Signal<ChatMessage[]>`         | Public     | Readonly: message list                 |
| `isLoading`          | `Signal<boolean>`               | Public     | Readonly: loading state                |
| `selectedProvider`   | `Signal<string>`                | Public     | Readonly: current provider             |
| `selectedModel`      | `Signal<string>`                | Public     | Readonly: current model                |
| `messageCount`       | `Signal<number>`                | Public     | Computed: message count                |
| `hasMessages`        | `Signal<boolean>`               | Public     | Computed: messages present             |
| `lastMessage`        | `Signal<ChatMessage \| null>`   | Public     | Computed: last message                 |

---

## 5. Usage in Components

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

## 6. Computed Signals for Derived State

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

## 7. Effects for Side Effects

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
| localStorage persistence      | Saving user preferences (provider, model)           |
| Logging                       | Logging state changes for debugging                 |
| Synchronization               | Triggering API calls in response to changes         |

---

## 8. Comparison with NgRx

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

For LocalMind v0.1.0, Angular Signals is the appropriate choice given the moderate complexity of the application state.

---

## 9. Guidelines

### When to use each primitive

| Primitive   | When to use                                                 |
|-------------|-------------------------------------------------------------|
| `signal()`  | Mutable state that needs to be tracked and updated          |
| `computed()`| Derived state that depends on other signals                 |
| `effect()`  | Side effects (localStorage, logging, reactive API calls)    |

### Naming conventions

| Pattern                     | Example                                |
|-----------------------------|----------------------------------------|
| Private writable signal     | `_messages`, `_isLoading`              |
| Public readonly signal      | `messages`, `isLoading`                |
| Computed signal             | `messageCount`, `hasMessages`          |
| Mutation method             | `addMessage()`, `setLoading()`         |

### Best practices

1. **Never expose writable signals**: always use `.asReadonly()`.
2. **Mutations through methods**: components should not call `.set()` or `.update()` directly.
3. **Computed for derived state**: never duplicate state; use `computed()`.
4. **Minimal effects**: one effect per side effect; avoid complex effects.
5. **Singleton store**: `providedIn: 'root'` to share state across components.
6. **Local state in the component**: for UI-only state (e.g., `userInput`), use normal component properties.
