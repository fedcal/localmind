# State Management con Angular Signals

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Framework:** Angular 21.0.0

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Primitive Signals](#2-primitive-signals)
3. [Pattern di Stato in LocalMind](#3-pattern-di-stato-in-localmind)
4. [ChatStore: Esempio Completo](#4-chatstore-esempio-completo)
5. [Utilizzo nei Componenti](#5-utilizzo-nei-componenti)
6. [Computed Signals per Stato Derivato](#6-computed-signals-per-stato-derivato)
7. [Effect per Side Effects](#7-effect-per-side-effects)
8. [Confronto con NgRx](#8-confronto-con-ngrx)
9. [Linee Guida](#9-linee-guida)

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

```typescript
import { signal } from '@angular/core';

// Creazione
const count = signal(0);

// Lettura (invocazione come funzione)
console.log(count());  // 0

// Scrittura
count.set(5);          // Imposta il valore
count.update(v => v + 1);  // Aggiorna in base al valore corrente
```

### computed()

Crea un valore derivato che si aggiorna automaticamente quando le dipendenze cambiano.

```typescript
import { signal, computed } from '@angular/core';

const items = signal<string[]>(['a', 'b', 'c']);
const itemCount = computed(() => items().length);  // Automaticamente 3
```

- **Lazy**: il valore viene calcolato solo alla prima lettura.
- **Cached**: il valore viene ricalcolato solo quando una dipendenza cambia.
- **Readonly**: non e' possibile assegnare un valore direttamente.

### effect()

Esegue un side effect quando uno o piu' Signals dipendenti cambiano.

```typescript
import { signal, effect } from '@angular/core';

const message = signal('Hello');

effect(() => {
  console.log('Message changed:', message());
});
```

- **Tracking automatico**: Angular rileva automaticamente quali Signals vengono letti nell'effect.
- **Esecuzione asincrona**: gli effects vengono eseguiti in modo asincrono dopo il rendering.
- **Cleanup**: il framework gestisce automaticamente la pulizia degli effects alla distruzione del componente/servizio.

### .asReadonly()

Converte un WritableSignal in un Signal di sola lettura.

```typescript
const _count = signal(0);
const count = _count.asReadonly();  // Signal<number> (non WritableSignal)

count();      // OK: lettura
// count.set(5)  // ERRORE di compilazione: 'set' non esiste su Signal<number>
```

---

## 3. Pattern di Stato in LocalMind

### Architettura dello store

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

### Regole del pattern

1. **Signals writable sono sempre privati**: prefisso `_` e visibilita' `private`.
2. **Signals readonly sono pubblici**: esposti tramite `.asReadonly()`.
3. **Le mutazioni avvengono solo tramite metodi**: i componenti non accedono mai direttamente ai signals writable.
4. **Store come servizio singleton**: `@Injectable({ providedIn: 'root' })`.
5. **Iniezione tramite `inject()`**: i componenti iniettano lo store con la funzione `inject()`.

---

## 4. ChatStore: Esempio Completo

### Implementazione

```typescript
import { Injectable, signal, computed } from '@angular/core';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({ providedIn: 'root' })
export class ChatStore {

  // === Signals privati (writable) ===

  private _messages = signal<ChatMessage[]>([]);
  private _isLoading = signal(false);
  private _selectedProvider = signal<string>('OLLAMA');
  private _selectedModel = signal<string>('llama3.2');

  // === Signals pubblici (readonly) ===

  readonly messages = this._messages.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly selectedProvider = this._selectedProvider.asReadonly();
  readonly selectedModel = this._selectedModel.asReadonly();

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

### Modello ChatMessage

```typescript
export interface ChatMessage {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}
```

### Signals dello store

| Signal               | Tipo                            | Visibilita' | Descrizione                            |
|----------------------|---------------------------------|-------------|----------------------------------------|
| `_messages`          | `WritableSignal<ChatMessage[]>` | Private     | Lista dei messaggi della conversazione |
| `_isLoading`         | `WritableSignal<boolean>`       | Private     | Flag di caricamento                    |
| `_selectedProvider`  | `WritableSignal<string>`        | Private     | Provider LLM selezionato               |
| `_selectedModel`     | `WritableSignal<string>`        | Private     | Modello LLM selezionato                |
| `messages`           | `Signal<ChatMessage[]>`         | Public      | Readonly: lista messaggi               |
| `isLoading`          | `Signal<boolean>`               | Public      | Readonly: stato di caricamento         |
| `selectedProvider`   | `Signal<string>`                | Public      | Readonly: provider corrente            |
| `selectedModel`      | `Signal<string>`                | Public      | Readonly: modello corrente             |
| `messageCount`       | `Signal<number>`                | Public      | Computed: numero messaggi              |
| `hasMessages`        | `Signal<boolean>`               | Public      | Computed: presenza messaggi            |
| `lastMessage`        | `Signal<ChatMessage \| null>`   | Public      | Computed: ultimo messaggio             |

---

## 5. Utilizzo nei Componenti

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

### Punti chiave

- **`messages()`**: invocazione del signal nel template per ottenere il valore corrente.
- **`@for ... track msg.id`**: nuova sintassi di iterazione Angular con tracking per ottimizzare il DOM update.
- **`@if (isLoading())`**: nuova sintassi condizionale Angular.
- **`[(ngModel)]`**: two-way binding per l'input utente (stato locale, non nello store).
- **`[disabled]="isLoading()"`**: binding reattivo basato sul signal di loading.

---

## 6. Computed Signals per Stato Derivato

I `computed()` signals derivano stato dai signals esistenti senza duplicazione:

```typescript
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
```

### Proprieta' dei computed

- **Lazy**: il calcolo avviene solo quando il valore viene letto.
- **Cached**: il valore e' ricalcolato solo se una dipendenza cambia.
- **Composable**: un computed puo' dipendere da altri computed.
- **Glitch-free**: Angular garantisce la coerenza dei valori derivati.

---

## 7. Effect per Side Effects

Gli `effect()` sono utilizzati per operazioni che devono avvenire in risposta a cambiamenti di stato:

```typescript
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
```

### Casi d'uso degli effects in LocalMind

| Caso d'uso                    | Descrizione                                         |
|-------------------------------|-----------------------------------------------------|
| Persistenza localStorage      | Salvataggio preferenze utente (provider, modello)   |
| Logging                       | Log dei cambiamenti di stato per debug              |
| Sincronizzazione              | Trigger di chiamate API in risposta a cambiamenti   |

---

## 8. Confronto con NgRx

| Aspetto                   | Angular Signals (LocalMind)       | NgRx                                                                   |
|---------------------------|-----------------------------------|------------------------------------------------------------------------|
| **Setup**                 | Zero configurazione               | Actions, Reducers, Effects, Selectors, Store module                    |
| **Boilerplate**           | Minimo (signal + metodo)          | Elevato (4+ file per feature)                                          |
| **Dipendenze npm**        | Nessuna                           | `@ngrx/store`, `@ngrx/effects`, `@ngrx/entity`, `@ngrx/store-devtools` |
| **Type safety**           | Nativa TypeScript                 | Richiede generics e configurazione                                     |
| **Curva di apprendimento**| 1-2 ore                           | 1-2 giorni                                                             |
| **DevTools**              | Angular DevTools (Signals tab)    | Redux DevTools (time-travel debugging)                                 |
| **Immutabilita'**         | Convenzione (`update` con spread) | Forzata dal framework                                                  |
| **Scalabilita'**          | Ottimale per app medio-piccole    | Necessario per app enterprise molto complesse                          |
| **Testing**               | Standard Angular testing          | Richiede testing specifico NgRx                                        |

### Quando preferire NgRx

NgRx diventa vantaggioso quando:
- Lo stato e' condiviso tra molti componenti disconnessi nell'albero.
- Si necessita di time-travel debugging.
- Le operazioni sullo stato sono complesse e composte.
- Il team ha gia' esperienza con Redux pattern.

Per LocalMind v0.1.0, Angular Signals e' la scelta appropriata data la complessita' moderata dello stato applicativo.

---

## 9. Linee Guida

### Quando usare ogni primitiva

| Primitiva   | Quando usarla                                               |
|-------------|-------------------------------------------------------------|
| `signal()`  | Stato mutabile che deve essere tracciato e aggiornato       |
| `computed()`| Stato derivato che dipende da altri signals                 |
| `effect()`  | Side effects (localStorage, logging, chiamate API reattive) |

### Convenzioni di naming

| Pattern                     | Esempio                                |
|-----------------------------|----------------------------------------|
| Signal privato writable     | `_messages`, `_isLoading`              |
| Signal pubblico readonly    | `messages`, `isLoading`                |
| Signal computed             | `messageCount`, `hasMessages`          |
| Metodo di mutazione         | `addMessage()`, `setLoading()`         |

### Best practices

1. **Non esporre mai signals writable**: sempre `.asReadonly()`.
2. **Mutazioni tramite metodi**: i componenti non devono chiamare `.set()` o `.update()` direttamente.
3. **Computed per stato derivato**: mai duplicare lo stato; usare `computed()`.
4. **Effects minimali**: un effect per side effect; evitare effects complessi.
5. **Store singleton**: `providedIn: 'root'` per condividere lo stato tra componenti.
6. **Stato locale nel componente**: per stato UI-only (es. `userInput`), usare proprieta' normali del componente.
