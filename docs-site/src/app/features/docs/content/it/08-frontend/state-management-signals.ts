export const content = `# State Management con Angular Signals

**Progetto:** LocalMind
**Versione:** 1.0.0
**Data:** 2026-02-18
**Framework:** Angular 21.0.0

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Primitive Signals](#2-primitive-signals)
3. [Pattern di Stato in LocalMind](#3-pattern-di-stato-in-localmind)
4. [ChatStore: Esempio Completo](#4-chatstore-esempio-completo)
5. [ThemeService: Secondo Esempio di Store Signal-Based](#5-themeservice-secondo-esempio-di-store-signal-based)
6. [Utilizzo nei Componenti](#6-utilizzo-nei-componenti)
7. [Computed Signals per Stato Derivato](#7-computed-signals-per-stato-derivato)
8. [Effect per Side Effects](#8-effect-per-side-effects)
9. [Confronto con NgRx](#9-confronto-con-ngrx)
10. [Linee Guida](#10-linee-guida)

---

## 1. Panoramica

Angular Signals e' una primitiva reattiva introdotta in Angular 16 e divenuta matura e stabile in Angular 21. In LocalMind, Signals rappresenta l'unico meccanismo di gestione dello stato applicativo. Non vengono utilizzate librerie esterne di state management (NgRx, Akita, NGXS, Elf).

### Principi di design

- **Semplicita'**: lo stato e' gestito tramite servizi Angular standard con Signals interni.
- **Incapsulamento**: i Signals writable sono privati; i componenti accedono solo a Signals readonly.
- **Zero dipendenze esterne**: nessuna libreria aggiuntiva richiesta.
- **Type safety**: il sistema di tipi TypeScript garantisce la coerenza dei dati reattivi.
- **Granularita' fine**: ogni signal traccia un singolo pezzo di stato, abilitando aggiornamenti mirati del DOM.

---

## 2. Primitive Signals

### signal()

Crea un valore reattivo mutabile. E' il building block fondamentale.

\`\`\`typescript
import { signal } from '@angular/core';

// Creazione
const count = signal(0);

// Lettura (invocazione come funzione)
console.log(count());  // 0

// Scrittura
count.set(5);          // Imposta il valore
count.update(v => v + 1);  // Aggiorna in base al valore corrente
\`\`\`

### computed()

Crea un valore derivato che si aggiorna automaticamente quando le dipendenze cambiano.

\`\`\`typescript
import { signal, computed } from '@angular/core';

const items = signal<string[]>(['a', 'b', 'c']);
const itemCount = computed(() => items().length);  // Automaticamente 3
\`\`\`

- **Lazy**: il valore viene calcolato solo alla prima lettura.
- **Cached**: il valore viene ricalcolato solo quando una dipendenza cambia.
- **Readonly**: non e' possibile assegnare un valore direttamente.

### effect()

Esegue un side effect quando uno o piu' Signals dipendenti cambiano.

\`\`\`typescript
import { signal, effect } from '@angular/core';

const message = signal('Hello');

effect(() => {
  console.log('Message changed:', message());
});
\`\`\`

- **Tracking automatico**: Angular rileva automaticamente quali Signals vengono letti nell'effect.
- **Esecuzione asincrona**: gli effects vengono eseguiti in modo asincrono dopo il rendering.
- **Cleanup**: il framework gestisce automaticamente la pulizia degli effects alla distruzione del componente/servizio.

### .asReadonly()

Converte un WritableSignal in un Signal di sola lettura.

\`\`\`typescript
const _count = signal(0);
const count = _count.asReadonly();  // Signal<number> (non WritableSignal)

count();      // OK: lettura
// count.set(5)  // ERRORE di compilazione: 'set' non esiste su Signal<number>
\`\`\`

---

## 3. Pattern di Stato in LocalMind

### Architettura dello store

\`\`\`
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
\`\`\`

### Regole del pattern

1. **Signals writable sono sempre privati**: prefisso \`_\` e visibilita' \`private\`.
2. **Signals readonly sono pubblici**: esposti tramite \`.asReadonly()\`.
3. **Le mutazioni avvengono solo tramite metodi**: i componenti non accedono mai direttamente ai signals writable.
4. **Store come servizio singleton**: \`@Injectable({ providedIn: 'root' })\`.
5. **Iniezione tramite \`inject()\`**: i componenti iniettano lo store con la funzione \`inject()\`.

---

## 4. ChatStore: Esempio Completo

### Implementazione

\`\`\`typescript
import { Injectable, signal, computed } from '@angular/core';
import { ChatMessage, ConversationSummary } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatStore {

  // === Signals privati (writable) ===

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

  // === Signals pubblici (readonly) ===

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

  // === Computed signals (stato derivato) ===

  readonly messageCount = computed(() => this._messages().length);
  readonly hasMessages = computed(() => this._messages().length > 0);
  readonly lastMessage = computed(() => {
    const msgs = this._messages();
    return msgs.length > 0 ? msgs[msgs.length - 1] : null;
  });

  // === Metodi di mutazione ===

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

  // --- Conversazioni ---

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

  // --- Paginazione ---

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

  // --- Tool Calling e RAG ---

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
\`\`\`

### Signals dello store

#### Signals writable privati

| Signal                      | Tipo                                       | Descrizione                                      |
|-----------------------------|---------------------------------------------|--------------------------------------------------|
| \`_messages\`                 | \`WritableSignal<ChatMessage[]>\`             | Lista dei messaggi della conversazione           |
| \`_isLoading\`                | \`WritableSignal<boolean>\`                   | Flag di caricamento                              |
| \`_selectedProvider\`         | \`WritableSignal<string>\`                    | Provider LLM selezionato                         |
| \`_selectedModel\`            | \`WritableSignal<string>\`                    | Modello LLM selezionato                          |
| \`_error\`                    | \`WritableSignal<string\\|null>\`              | Messaggio di errore corrente                     |
| \`_currentConversationId\`    | \`WritableSignal<string\\|null>\`              | ID conversazione corrente                        |
| \`_currentConversationTitle\` | \`WritableSignal<string\\|null>\`              | Titolo conversazione corrente                    |
| \`_conversations\`            | \`WritableSignal<ConversationSummary[]>\`     | Lista delle conversazioni                        |
| \`_conversationsLoading\`     | \`WritableSignal<boolean>\`                   | Flag caricamento conversazioni                   |
| \`_currentSystemPrompt\`      | \`WritableSignal<string\\|null>\`              | System prompt corrente                           |
| \`_toolCallingEnabled\`       | \`WritableSignal<boolean>\`                   | Tool calling abilitato                           |
| \`_ragEnabled\`               | \`WritableSignal<boolean>\`                   | RAG abilitato                                    |
| \`_maxContextMessages\`       | \`WritableSignal<number\\|null>\`              | Limite messaggi di contesto                      |
| \`_isStreaming\`              | \`WritableSignal<boolean>\`                   | Streaming SSE in corso                           |
| \`_advancedMode\`             | \`WritableSignal<boolean>\`                   | Modalita' avanzata (persistente localStorage)    |
| \`_filterTag\`                | \`WritableSignal<string\\|null>\`              | Tag filtro conversazioni                         |
| \`_searchQuery\`              | \`WritableSignal<string>\`                    | Query di ricerca conversazioni                   |
| \`_currentPage\`              | \`WritableSignal<number>\`                    | Pagina corrente paginazione                      |
| \`_totalPages\`               | \`WritableSignal<number>\`                    | Totale pagine                                    |
| \`_totalElements\`            | \`WritableSignal<number>\`                    | Totale elementi                                  |
| \`_hasMore\`                  | \`WritableSignal<boolean>\`                   | Flag per ulteriori pagine                        |

#### Signals readonly pubblici

| Signal                     | Tipo                              | Descrizione                                      |
|----------------------------|-----------------------------------|--------------------------------------------------|
| \`messages\`                 | \`Signal<ChatMessage[]>\`           | Lista messaggi della conversazione               |
| \`isLoading\`                | \`Signal<boolean>\`                 | Stato di caricamento                             |
| \`selectedProvider\`         | \`Signal<string>\`                  | Provider LLM selezionato                         |
| \`selectedModel\`            | \`Signal<string>\`                  | Modello LLM selezionato                          |
| \`error\`                    | \`Signal<string\\|null>\`            | Messaggio di errore corrente                     |
| \`currentConversationId\`    | \`Signal<string\\|null>\`            | ID conversazione corrente                        |
| \`currentConversationTitle\` | \`Signal<string\\|null>\`            | Titolo conversazione corrente                    |
| \`conversations\`            | \`Signal<ConversationSummary[]>\`   | Lista delle conversazioni                        |
| \`conversationsLoading\`     | \`Signal<boolean>\`                 | Flag caricamento conversazioni                   |
| \`currentSystemPrompt\`      | \`Signal<string\\|null>\`            | System prompt corrente                           |
| \`toolCallingEnabled\`       | \`Signal<boolean>\`                 | Tool calling abilitato                           |
| \`ragEnabled\`               | \`Signal<boolean>\`                 | RAG abilitato                                    |
| \`maxContextMessages\`       | \`Signal<number\\|null>\`            | Limite messaggi di contesto                      |
| \`isStreaming\`              | \`Signal<boolean>\`                 | Streaming SSE in corso                           |
| \`advancedMode\`             | \`Signal<boolean>\`                 | Modalita' avanzata                               |
| \`filterTag\`                | \`Signal<string\\|null>\`            | Tag filtro conversazioni                         |
| \`searchQuery\`              | \`Signal<string>\`                  | Query di ricerca conversazioni                   |
| \`currentPage\`              | \`Signal<number>\`                  | Pagina corrente paginazione                      |
| \`totalPages\`               | \`Signal<number>\`                  | Totale pagine                                    |
| \`totalElements\`            | \`Signal<number>\`                  | Totale elementi                                  |
| \`hasMore\`                  | \`Signal<boolean>\`                 | Flag per ulteriori pagine                        |

#### Computed signals

| Signal         | Tipo                            | Descrizione                            |
|----------------|---------------------------------|----------------------------------------|
| \`messageCount\` | \`Signal<number>\`                | Numero di messaggi nella conversazione |
| \`hasMessages\`  | \`Signal<boolean>\`               | Indica se ci sono messaggi             |
| \`lastMessage\`  | \`Signal<ChatMessage \\| null>\`   | Ultimo messaggio della conversazione   |

### Metodi di mutazione

| Metodo                       | Parametri                                              | Descrizione                                              |
|------------------------------|--------------------------------------------------------|----------------------------------------------------------|
| \`addMessage\`                 | \`message: ChatMessage\`                                 | Aggiunge un messaggio alla lista                         |
| \`setLoading\`                 | \`value: boolean\`                                       | Imposta lo stato di caricamento                          |
| \`clearMessages\`              | -                                                      | Svuota la lista messaggi                                 |
| \`setProvider\`                | \`provider: string\`                                     | Imposta il provider LLM                                  |
| \`setModel\`                   | \`model: string\`                                        | Imposta il modello LLM                                   |
| \`setError\`                   | \`error: string \\| null\`                                | Imposta il messaggio di errore                           |
| \`addUserMessage\`             | \`content: string\`                                      | Aggiunge un messaggio utente con UUID auto-generato      |
| \`addAssistantMessage\`        | \`content: string\`                                      | Aggiunge un messaggio assistente con UUID auto-generato  |
| \`setStreaming\`               | \`value: boolean\`                                       | Imposta lo stato di streaming SSE                        |
| \`addStreamingAssistantMessage\` | -                                                    | Aggiunge un messaggio assistente vuoto per lo streaming  |
| \`appendTokenToLastMessage\`   | \`token: string\`                                        | Appende un token all'ultimo messaggio assistente         |
| \`setCurrentConversation\`     | \`id: string \\| null, title: string \\| null\`            | Imposta la conversazione corrente                        |
| \`updateConversationTitle\`    | \`title: string\`                                        | Aggiorna il titolo della conversazione corrente          |
| \`loadConversationMessages\`   | \`messages: ChatMessage[]\`                              | Carica i messaggi di una conversazione                   |
| \`removeConversation\`         | \`id: string\`                                           | Rimuove una conversazione dalla lista                    |
| \`setPaginationInfo\`          | \`page, totalPages, totalElements, hasMore\`             | Imposta le informazioni di paginazione                   |
| \`appendConversations\`        | \`conversations: ConversationSummary[]\`                 | Aggiunge conversazioni alla lista esistente              |
| \`resetPagination\`            | -                                                      | Resetta paginazione e svuota la lista conversazioni      |
| \`toggleAdvancedMode\`         | -                                                      | Alterna la modalita' avanzata (persiste in localStorage) |
| \`toggleToolCalling\`          | -                                                      | Alterna l'abilitazione del tool calling                  |
| \`toggleRag\`                  | -                                                      | Alterna l'abilitazione del RAG                           |
| \`setSystemPrompt\`            | \`prompt: string \\| null\`                               | Imposta il system prompt                                 |
| \`setMaxContextMessages\`      | \`max: number \\| null\`                                  | Imposta il limite di messaggi di contesto                |

---

## 5. ThemeService: Secondo Esempio di Store Signal-Based

### Panoramica

Il ThemeService gestisce il tema dell'applicazione (light/dark) tramite Signals, seguendo lo stesso pattern del ChatStore. Rappresenta un esempio di store minimo che combina signals, computed, effect e persistenza localStorage.

### Implementazione

\`\`\`typescript
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
\`\`\`

### Signals del ThemeService

| Signal         | Tipo                          | Visibilita' | Descrizione                        |
|----------------|-------------------------------|-------------|------------------------------------|
| \`_currentTheme\`| \`WritableSignal<'light'\\|'dark'>\` | Private | Tema corrente                      |
| \`currentTheme\` | \`Signal<'light'\\|'dark'>\`     | Public      | Readonly: tema corrente            |
| \`isDark\`       | \`Signal<boolean>\`             | Public      | Computed: indica se il tema e' dark |

### Funzionamento

1. **Inizializzazione**: il tema iniziale viene letto da localStorage. Se non presente, utilizza la preferenza del sistema operativo tramite \`matchMedia('(prefers-color-scheme: dark)')\`.
2. **Effect reattivo**: ogni volta che il signal \`_currentTheme\` cambia, l'effect aggiorna l'attributo \`data-theme\` sul \`<html>\` e persiste la scelta in localStorage.
3. **Toggle**: il metodo \`toggle()\` alterna tra \`'light'\` e \`'dark'\`, triggerando automaticamente l'effect.

Questo pattern si applica a qualsiasi stato globale che necessita di persistenza e side effects: preferenze lingua, layout sidebar, dimensione font, etc.

---

## 6. Utilizzo nei Componenti

### ChatPageComponent

\`\`\`typescript
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

  // Signals esposti al template
  messages = this.chatStore.messages;
  isLoading = this.chatStore.isLoading;
  selectedProvider = this.chatStore.selectedProvider;
  selectedModel = this.chatStore.selectedModel;

  // Stato locale del componente
  userInput = '';

  sendMessage(): void {
    if (!this.userInput.trim() || this.chatStore.isLoading()) return;

    const message = this.userInput.trim();
    this.userInput = '';

    // Aggiunge il messaggio utente allo store
    this.chatStore.addUserMessage(message);
    this.chatStore.setLoading(true);

    // Chiamata API
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
\`\`\`

### Template (chat-page.component.html)

\`\`\`html
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
\`\`\`

### Punti chiave

- **\`messages()\`**: invocazione del signal nel template per ottenere il valore corrente.
- **\`@for ... track msg.id\`**: nuova sintassi di iterazione Angular con tracking per ottimizzare il DOM update.
- **\`@if (isLoading())\`**: nuova sintassi condizionale Angular.
- **\`[(ngModel)]\`**: two-way binding per l'input utente (stato locale, non nello store).
- **\`[disabled]="isLoading()"\`**: binding reattivo basato sul signal di loading.

---

## 7. Computed Signals per Stato Derivato

I \`computed()\` signals derivano stato dai signals esistenti senza duplicazione:

\`\`\`typescript
// Numero di messaggi
readonly messageCount = computed(() => this._messages().length);

// Presenza di messaggi
readonly hasMessages = computed(() => this._messages().length > 0);

// Ultimo messaggio
readonly lastMessage = computed(() => {
  const msgs = this._messages();
  return msgs.length > 0 ? msgs[msgs.length - 1] : null;
});

// Esempio nel template
@if (hasMessages()) {
  <span>{{ messageCount() }} messaggi</span>
}
\`\`\`

### Proprieta' dei computed

- **Lazy**: il calcolo avviene solo quando il valore viene letto.
- **Cached**: il valore e' ricalcolato solo se una dipendenza cambia.
- **Composable**: un computed puo' dipendere da altri computed.
- **Glitch-free**: Angular garantisce la coerenza dei valori derivati.

---

## 8. Effect per Side Effects

Gli \`effect()\` sono utilizzati per operazioni che devono avvenire in risposta a cambiamenti di stato:

\`\`\`typescript
import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ChatStore {

  private _selectedProvider = signal<string>('OLLAMA');

  constructor() {
    // Side effect: logga il cambio di provider
    effect(() => {
      console.log('Provider cambiato:', this._selectedProvider());
    });

    // Side effect: salva la preferenza in localStorage
    effect(() => {
      localStorage.setItem('selectedProvider', this._selectedProvider());
    });
  }
}
\`\`\`

### Casi d'uso degli effects in LocalMind

| Caso d'uso                    | Descrizione                                         |
|-------------------------------|-----------------------------------------------------|
| Persistenza localStorage      | Salvataggio preferenze utente (provider, modello, tema, modalita' chat) |
| Logging                       | Log dei cambiamenti di stato per debug              |
| Sincronizzazione DOM          | Aggiornamento attributi HTML (es. \`data-theme\`)     |
| Sincronizzazione              | Trigger di chiamate API in risposta a cambiamenti   |

---

## 9. Confronto con NgRx

| Aspetto                   | Angular Signals (LocalMind)       | NgRx                                                                   |
|---------------------------|-----------------------------------|------------------------------------------------------------------------|
| **Setup**                 | Zero configurazione               | Actions, Reducers, Effects, Selectors, Store module                    |
| **Boilerplate**           | Minimo (signal + metodo)          | Elevato (4+ file per feature)                                          |
| **Dipendenze npm**        | Nessuna                           | \`@ngrx/store\`, \`@ngrx/effects\`, \`@ngrx/entity\`, \`@ngrx/store-devtools\` |
| **Type safety**           | Nativa TypeScript                 | Richiede generics e configurazione                                     |
| **Curva di apprendimento**| 1-2 ore                           | 1-2 giorni                                                             |
| **DevTools**              | Angular DevTools (Signals tab)    | Redux DevTools (time-travel debugging)                                 |
| **Immutabilita'**         | Convenzione (\`update\` con spread) | Forzata dal framework                                                  |
| **Scalabilita'**          | Ottimale per app medio-piccole    | Necessario per app enterprise molto complesse                          |
| **Testing**               | Standard Angular testing          | Richiede testing specifico NgRx                                        |

### Quando preferire NgRx

NgRx diventa vantaggioso quando:
- Lo stato e' condiviso tra molti componenti disconnessi nell'albero.
- Si necessita di time-travel debugging.
- Le operazioni sullo stato sono complesse e composte.
- Il team ha gia' esperienza con Redux pattern.

Per LocalMind v1.0.0, Angular Signals e' la scelta appropriata data la complessita' moderata dello stato applicativo. Il ChatStore dimostra che anche con 20+ signals, il pattern resta gestibile e leggibile.

---

## 10. Linee Guida

### Quando usare ogni primitiva

| Primitiva   | Quando usarla                                               |
|-------------|-------------------------------------------------------------|
| \`signal()\`  | Stato mutabile che deve essere tracciato e aggiornato       |
| \`computed()\`| Stato derivato che dipende da altri signals                 |
| \`effect()\`  | Side effects (localStorage, logging, DOM sync, chiamate API reattive) |

### Convenzioni di naming

| Pattern                     | Esempio                                        |
|-----------------------------|------------------------------------------------|
| Signal privato writable     | \`_messages\`, \`_isLoading\`, \`_isStreaming\`       |
| Signal pubblico readonly    | \`messages\`, \`isLoading\`, \`isStreaming\`          |
| Signal computed             | \`messageCount\`, \`hasMessages\`, \`isDark\`         |
| Metodo di mutazione         | \`addMessage()\`, \`setLoading()\`, \`toggle()\`     |

### Best practices

1. **Non esporre mai signals writable**: sempre \`.asReadonly()\`.
2. **Mutazioni tramite metodi**: i componenti non devono chiamare \`.set()\` o \`.update()\` direttamente.
3. **Computed per stato derivato**: mai duplicare lo stato; usare \`computed()\`.
4. **Effects minimali**: un effect per side effect; evitare effects complessi.
5. **Store singleton**: \`providedIn: 'root'\` per condividere lo stato tra componenti.
6. **Stato locale nel componente**: per stato UI-only (es. \`userInput\`), usare proprieta' normali del componente.
7. **Persistenza tramite effect**: per stato che deve sopravvivere al refresh (tema, modalita' chat), usare \`effect()\` con \`localStorage\`.
8. **Inizializzazione da localStorage**: leggere il valore iniziale nel signal constructor per ripristinare lo stato al caricamento.
`;
