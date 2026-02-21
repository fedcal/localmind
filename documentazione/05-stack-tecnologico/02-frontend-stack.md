# Stack Tecnologico Frontend

**Progetto:** LocalMind
**Versione:** 1.0.0
**Data:** 2026-02-18
**Modulo:** localmind-frontend

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Angular 21.0.0](#2-angular-2100)
3. [TypeScript 5.9.2](#3-typescript-592)
4. [RxJS 7.8.0](#4-rxjs-780)
5. [Angular Signals](#5-angular-signals)
6. [Standalone Components](#6-standalone-components)
7. [Angular Router](#7-angular-router)
8. [SCSS](#8-scss)
9. [Vitest 4.0.8](#9-vitest-408)
10. [Environment Files](#10-environment-files)
11. [Dark Mode](#11-dark-mode)
12. [SSE Streaming Chat](#12-sse-streaming-chat)
13. [Form Validation](#13-form-validation)
14. [Internazionalizzazione (i18n)](#14-internazionalizzazione-i18n)
15. [Tabella Riepilogativa Dipendenze](#15-tabella-riepilogativa-dipendenze)

---

## 1. Panoramica

Il frontend di LocalMind e' un'applicazione Single Page Application (SPA) costruita con Angular 21, l'ultima versione stabile del framework. L'architettura adotta un approccio moderno basato su Standalone Components (senza NgModule), Angular Signals per la gestione dello stato reattivo e lazy loading per le feature routes. Non sono utilizzate librerie di state management esterne (NgRx, Akita, NGXS): la reattivita' e' gestita interamente tramite il sistema nativo di Signals.

---

## 2. Angular 21.0.0

| Proprieta'  | Valore                                               |
|-------------|------------------------------------------------------|
| **Nome**    | Angular                                              |
| **Versione**| 21.0.0                                               |
| **Scopo**   | Framework applicativo frontend                       |

### Motivazione della scelta

Angular 21 e' stato selezionato come framework frontend per le seguenti ragioni:

- **Enterprise-grade**: framework completo e opinionated, con struttura progettuale chiara e convenzioni consolidate.
- **TypeScript nativo**: Angular e' scritto in TypeScript e lo richiede nativamente, garantendo type safety end-to-end.
- **CLI eccellente**: Angular CLI (`ng`) fornisce generazione di codice, build ottimizzato, test runner e server di sviluppo.
- **Component architecture**: sistema di componenti maturo con template dichiarativi, data binding bidirezionale e dependency injection.
- **Standalone Components by default**: a partire da Angular 17+, i componenti standalone sono il default, eliminando la necessita' di NgModule.
- **Signals built-in**: Angular 21 include il sistema Signals come primitiva reattiva di prima classe, matura e stabile.

### Funzionalita' Angular 21 utilizzate

| Funzionalita'                 | Descrizione                                                |
|-------------------------------|-------------------------------------------------------------|
| Standalone Components         | Componenti autonomi senza NgModule                          |
| Signals                       | `signal()`, `computed()`, `effect()` per stato reattivo     |
| Control Flow (`@for`, `@if`)  | Nuova sintassi di template per condizionali e iterazioni    |
| `inject()`                    | Funzione per dependency injection nei componenti            |
| `provideRouter()`             | Configurazione router tramite function-based provider       |
| `provideHttpClient()`         | Client HTTP configurato tramite provider                    |
| `withComponentInputBinding()` | Binding automatico dei route parameters ai component inputs |

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                                                                                  |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------|
| React       | Eccellente ecosistema, ma meno opinionated; richiede scelte architetturali aggiuntive per routing, state management e form handling |
| Vue.js      | Framework progressivo e accessibile, ma ecosistema enterprise meno consolidato rispetto ad Angular                                  |
| Svelte      | Approccio innovativo con compilazione, ma ecosistema meno maturo per applicazioni enterprise complesse                              |

---

## 3. TypeScript 5.9.2

| Proprieta'  | Valore                                         |
|-------------|------------------------------------------------|
| **Nome**    | TypeScript                                     |
| **Versione**| 5.9.2                                          |
| **Scopo**   | Linguaggio tipizzato per sviluppo frontend     |

### Motivazione della scelta

TypeScript e' il linguaggio nativo di Angular. La versione 5.9.2 e' utilizzata per:

- **Type safety**: rilevamento errori in fase di compilazione, non a runtime.
- **IDE support**: autocompletamento, refactoring, navigazione codice in editor moderni (VS Code, WebStorm).
- **Union types e discriminated unions**: per modellare stati dell'applicazione (es. status documento: `'PENDING' | 'PROCESSING' | 'INDEXED' | 'ERROR'`).
- **Strict mode**: configurazione `strict: true` in `tsconfig.json` per massima sicurezza del tipo.
- **Template type checking**: Angular verifica staticamente i tipi nei template HTML.

---

## 4. RxJS 7.8.0

| Proprieta'  | Valore                                         |
|-------------|------------------------------------------------|
| **Nome**    | RxJS (Reactive Extensions for JavaScript)      |
| **Versione**| 7.8.0                                          |
| **Scopo**   | Stream asincroni e integrazione HTTP client    |

### Utilizzo nel progetto

RxJS e' una dipendenza core di Angular, utilizzata nel progetto per:

- **HttpClient**: tutte le chiamate HTTP restituiscono `Observable<T>`, gestiti con operatori RxJS.
- **Operatori utilizzati**: `map`, `catchError`, `tap`, `switchMap`, `finalize`.
- **Integrazione con Signals**: i risultati delle chiamate HTTP vengono convertiti in signals tramite pattern di sottoscrizione nei service.
- **Error handling**: gestione centralizzata degli errori HTTP tramite interceptor.

> **Nota**: per la gestione dello stato applicativo locale (messaggi chat, loading state, provider selezionato), il progetto utilizza Angular Signals anziche' Observable/BehaviorSubject. RxJS rimane in uso esclusivamente per operazioni asincrone HTTP.

---

## 5. Angular Signals

| Proprieta'  | Valore                                               |
|-------------|------------------------------------------------------|
| **Nome**    | Angular Signals                                      |
| **Versione**| Integrato in Angular 21.0.0                          |
| **Scopo**   | Gestione stato reattivo senza librerie esterne       |

### Descrizione

Angular Signals e' una primitiva reattiva introdotta in Angular 16 e divenuta matura e stabile in Angular 21. Rappresenta il meccanismo principale di gestione dello stato in LocalMind.

### Primitive utilizzate

| Primitiva     | Descrizione                                                      | Esempio                                                  |
|---------------|------------------------------------------------------------------|----------------------------------------------------------|
| `signal()`    | Crea un valore reattivo mutabile                                 | `_messages = signal<ChatMessage[]>([])`                  |
| `computed()`  | Crea un valore derivato che si aggiorna automaticamente          | `messageCount = computed(() => this._messages().length)` |
| `effect()`    | Esegue side effect quando i segnali dipendenti cambiano          | `effect(() => console.log(this._messages()))`            |
| `.asReadonly()`| Espone un signal in sola lettura                                | `messages = this._messages.asReadonly()`                 |

### Pattern di stato in LocalMind

Il pattern adottato prevede:
- **Injectable store class** (`providedIn: 'root'`): servizio singleton che incapsula lo stato.
- **Signal privati writable**: `_messages = signal<ChatMessage[]>([])` - mutabili solo internamente.
- **Signal pubblici readonly**: `messages = this._messages.asReadonly()` - esposti ai componenti.
- **Metodi di mutazione**: `addMessage()`, `setLoading()`, `clearMessages()` - unico punto di modifica dello stato.

### Vantaggi rispetto a NgRx

| Aspetto               | Angular Signals (LocalMind)    | NgRx                                  |
|-----------------------|--------------------------------|---------------------------------------|
| Boilerplate           | Zero                           | Actions, Reducers, Effects, Selectors |
| Dipendenze esterne    | Nessuna                        | `@ngrx/store`, `@ngrx/effects`, ecc.  |
| Type safety           | Nativa                         | Richiede configurazione               |
| Curva di apprendimento| Bassa                          | Alta                                  |
| Adeguatezza           | Ottimale per app medio-piccole | Necessario per app molto complesse    |

---

## 6. Standalone Components

| Proprieta'  | Valore                                         |
|-------------|------------------------------------------------|
| **Nome**    | Angular Standalone Components                  |
| **Versione**| Default da Angular 17+                         |
| **Scopo**   | Componenti autonomi senza NgModule             |

### Descrizione

In Angular 21, i Standalone Components sono il default. LocalMind non utilizza NgModule: ogni componente dichiara le proprie dipendenze nell'array `imports` del decoratore `@Component`.

### Vantaggi

- **Tree-shakable**: solo i componenti effettivamente utilizzati vengono inclusi nel bundle.
- **Lazy loadable**: ogni componente puo' essere caricato on-demand tramite `loadComponent`.
- **Self-contained**: ogni componente dichiara esplicitamente le proprie dipendenze (pipe, direttive, altri componenti).
- **Semplicita'**: eliminazione della complessita' di NgModule e delle dichiarazioni ridondanti.

### Esempio di Standalone Component

```typescript
@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.scss'
})
export class ChatPageComponent {
  private chatStore = inject(ChatStore);
  messages = this.chatStore.messages;
  isLoading = this.chatStore.isLoading;
}
```

---

## 7. Angular Router

| Proprieta'  | Valore                                         |
|-------------|------------------------------------------------|
| **Nome**    | Angular Router                                 |
| **Versione**| Integrato in Angular 21.0.0                    |
| **Scopo**   | Navigazione e lazy loading per feature         |

### Caratteristiche utilizzate

- **Lazy loading**: ogni feature e' un bundle separato caricato on-demand tramite `loadChildren` o `loadComponent`.
- **`withComponentInputBinding()`**: i parametri di route vengono automaticamente mappati sugli `@Input()` dei componenti.
- **`provideRouter()`**: configurazione del router tramite function-based provider in `app.config.ts`.
- **Wildcard redirect**: tutte le rotte non riconosciute sono reindirizzate a `/chat`.
- **LayoutComponent**: componente shell che contiene la navigazione laterale e il router-outlet.

---

## 8. SCSS

| Proprieta'  | Valore                                         |
|-------------|------------------------------------------------|
| **Nome**    | SCSS (Sassy CSS)                               |
| **Versione**| Integrato in Angular CLI                       |
| **Scopo**   | Preprocessore CSS                              |

### Utilizzo

- File di stili globali: `src/styles.scss`.
- Ogni componente ha il proprio file `.scss` con stili incapsulati (View Encapsulation di Angular).
- Variabili SCSS per temi e colori condivisi.

---

## 9. Vitest 4.0.8

| Proprieta'  | Valore                                         |
|-------------|------------------------------------------------|
| **Nome**    | Vitest                                         |
| **Versione**| 4.0.8                                          |
| **Scopo**   | Test runner per unit test e component test     |

### Motivazione della scelta

Vitest sostituisce il tradizionale stack Karma/Jasmine come test runner del progetto:

- **Performance**: esecuzione dei test significativamente piu' veloce rispetto a Karma grazie a Vite.
- **ESM nativo**: supporto nativo per ES Modules senza necessita' di trasformazione.
- **API compatibile**: API simile a Jest, facilitando la migrazione.
- **Watch mode**: re-esecuzione istantanea dei test modificati.
- **Integrazione Angular**: compatibile con `@angular/core/testing` e component test utilities.

---

## 10. Environment Files

| File                      | Ambiente    | Descrizione                          |
|---------------------------|-------------|--------------------------------------|
| `environment.ts`          | Development | Configurazione per sviluppo locale   |
| `environment.prod.ts`     | Production  | Configurazione per produzione        |

### environment.ts (Development)

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1'
};
```

### environment.prod.ts (Production)

```typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1'
};
```

> **Nota**: in produzione, l'URL API e' relativo (`/api/v1`) per consentire il deploy dietro un reverse proxy sulla stessa origin del frontend.

---

## 11. Dark Mode

LocalMind supporta i temi light e dark tramite CSS custom properties e un `ThemeService` signal-based.

**ThemeService** (`core/services/theme.service.ts`):
- Signal `currentTheme` (`'light'` | `'dark'`) con persistenza localStorage
- Default da `prefers-color-scheme` media query del browser
- `effect()` che applica attributo `data-theme` su `<html>` e salva in localStorage
- Metodi: `toggle()`, `setTheme()`, `isDark()` (computed)

**CSS Custom Properties** (`styles.scss`):
- ~40 variabili semantiche definite in `:root` (light mode) e override in `[data-theme="dark"]`
- Categorie: colori primari, neutri, status (success/error/warning/info), UI (card-bg, input-bg, hover-bg, overlay)
- Tutti i componenti usano `var(--color-*)` invece di colori hardcoded

**ThemeToggleComponent** (`shared/components/theme-toggle/`):
- Icona sole/luna SVG con animazione di rotazione
- Integrato nel footer del sidebar (layout.component.ts)
- Supporta stato collassato del sidebar

---

## 12. SSE Streaming Chat

Il frontend supporta lo streaming delle risposte tramite SSE usando `fetch()` + `ReadableStream`.

**ChatStreamService** (`features/chat/services/chat-stream.service.ts`):
- Usa `fetch()` con body POST (non EventSource, che supporta solo GET)
- Parse manuale delle linee SSE (`event:` e `data:`)
- Ritorna `Observable<SSEEvent>` con tipi: `conversation`, `token`, `metadata`, `done`, `error`
- Fallback automatico a chat sincrona se lo stream fallisce

**Integrazione ChatStore**:
- Signal `_isStreaming`: indica streaming in corso
- `addStreamingAssistantMessage()`: aggiunge messaggio vuoto ASSISTANT
- `appendTokenToLastMessage(token)`: appende token all'ultimo messaggio progressivamente

---

## 13. Form Validation

Validazione template-driven con Angular Forms su tutti i form dell'applicazione.

**Stili globali** (`styles.scss`):
- `input.ng-invalid.ng-touched`: bordo rosso
- `input.ng-valid.ng-touched`: bordo verde
- `.form-error`: testo errore rosso sotto il campo
- `.char-counter`: contatore caratteri con varianti `.near-limit` e `.at-limit`

**Form validati**:
| Componente | Campi | Validazioni |
|---|---|---|
| Settings | name, baseUrl, apiKey | required, minlength, maxlength |
| Folders | path | required, minlength=2 |
| MCP Servers | name, command/url | required, minlength=2 |
| Login | password, confirmPassword | required, minlength=4, match |
| Chat | systemPrompt | maxlength=5000, char counter |

---

## 14. Internazionalizzazione (i18n)

Sistema di traduzione custom basato su Signals.

**TranslationService** (`core/i18n/translation.service.ts`):
- Signal `currentLang` (`'it'` | `'en'`) con persistenza localStorage
- Caricamento lazy dei file JSON da `assets/i18n/{lang}.json`
- Supporto interpolazione: `{{ 'KEY' | translate:{ param: value } }}`

**TranslatePipe** (`core/i18n/translate.pipe.ts`):
- Pipe standalone per traduzione nei template
- Usata in tutti i componenti standalone

**LanguageSwitcherComponent** (`shared/components/language-switcher/`):
- Toggle IT/EN integrato nel sidebar footer

---

## 15. Tabella Riepilogativa Dipendenze

| Dipendenza           | Versione | Scopo                                                     |
|----------------------|----------|-----------------------------------------------------------|
| Angular              | 21.0.0   | Framework applicativo                                     |
| TypeScript           | 5.9.2    | Linguaggio tipizzato                                      |
| RxJS                 | 7.8.0    | Stream asincroni e HTTP                                   |
| Angular Signals      | built-in | State management reattivo                                 |
| Angular Router       | built-in | Navigazione e lazy loading                                |
| Angular HttpClient   | built-in | Client HTTP con interceptor                               |
| Angular Forms        | built-in | Template-driven e reactive forms                          |
| SCSS                 | built-in | Preprocessore CSS                                         |
| Vitest               | 4.0.8    | Test runner                                               |
| Zone.js              | built-in | Change detection (progressivamente sostituito da Signals) |
