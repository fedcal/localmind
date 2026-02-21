export const content = `# Frontend Technology Stack

**Project:** LocalMind
**Version:** 1.0.0
**Date:** 2026-02-18
**Module:** localmind-frontend

---

## Table of Contents

1. [Overview](#1-overview)
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
14. [Internationalization (i18n)](#14-internationalization-i18n)
15. [Dependency Summary Table](#15-dependency-summary-table)

---

## 1. Overview

The LocalMind frontend is a Single Page Application (SPA) built with Angular 21, the latest stable version of the framework. The architecture adopts a modern approach based on Standalone Components (without NgModule), Angular Signals for reactive state management, and lazy loading for feature routes. No external state management libraries are used (NgRx, Akita, NGXS): reactivity is handled entirely through the native Signals system.

---

## 2. Angular 21.0.0

| Property    | Value                                                |
|-------------|------------------------------------------------------|
| **Name**    | Angular                                              |
| **Version** | 21.0.0                                               |
| **Purpose** | Frontend application framework                       |

### Rationale

Angular 21 was selected as the frontend framework for the following reasons:

- **Enterprise-grade**: complete and opinionated framework, with a clear project structure and established conventions.
- **Native TypeScript**: Angular is written in TypeScript and natively requires it, ensuring end-to-end type safety.
- **Excellent CLI**: Angular CLI (\`ng\`) provides code generation, optimized builds, test runner, and development server.
- **Component architecture**: mature component system with declarative templates, two-way data binding, and dependency injection.
- **Standalone Components by default**: starting from Angular 17+, standalone components are the default, eliminating the need for NgModule.
- **Built-in Signals**: Angular 21 includes the Signals system as a first-class reactive primitive, mature and stable.

### Angular 21 Features Used

| Feature                         | Description                                                 |
|---------------------------------|-------------------------------------------------------------|
| Standalone Components           | Self-contained components without NgModule                  |
| Signals                         | \`signal()\`, \`computed()\`, \`effect()\` for reactive state     |
| Control Flow (\`@for\`, \`@if\`)    | New template syntax for conditionals and iterations         |
| \`inject()\`                      | Function for dependency injection in components             |
| \`provideRouter()\`               | Router configuration via function-based provider            |
| \`provideHttpClient()\`           | HTTP client configured via provider                         |
| \`withComponentInputBinding()\`   | Automatic binding of route parameters to component inputs   |

### Alternatives Considered

| Alternative | Reason for Rejection                                                                                                                    |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| React       | Excellent ecosystem, but less opinionated; requires additional architectural choices for routing, state management, and form handling   |
| Vue.js      | Progressive and accessible framework, but enterprise ecosystem less established compared to Angular                                     |
| Svelte      | Innovative approach with compilation, but less mature ecosystem for complex enterprise applications                                     |

---

## 3. TypeScript 5.9.2

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | TypeScript                                     |
| **Version** | 5.9.2                                          |
| **Purpose** | Typed language for frontend development        |

### Rationale

TypeScript is Angular's native language. Version 5.9.2 is used for:

- **Type safety**: error detection at compile time, not at runtime.
- **IDE support**: autocompletion, refactoring, code navigation in modern editors (VS Code, WebStorm).
- **Union types and discriminated unions**: for modeling application states (e.g., document status: \`'PENDING' | 'PROCESSING' | 'INDEXED' | 'ERROR'\`).
- **Strict mode**: \`strict: true\` configuration in \`tsconfig.json\` for maximum type safety.
- **Template type checking**: Angular statically verifies types in HTML templates.

---

## 4. RxJS 7.8.0

| Property    | Value                                            |
|-------------|--------------------------------------------------|
| **Name**    | RxJS (Reactive Extensions for JavaScript)        |
| **Version** | 7.8.0                                            |
| **Purpose** | Asynchronous streams and HTTP client integration |

### Usage in the Project

RxJS is a core Angular dependency, used in the project for:

- **HttpClient**: all HTTP calls return \`Observable<T>\`, handled with RxJS operators.
- **Operators used**: \`map\`, \`catchError\`, \`tap\`, \`switchMap\`, \`finalize\`.
- **Integration with Signals**: HTTP call results are converted to signals via subscription patterns in services.
- **Error handling**: centralized HTTP error handling through an interceptor.

> **Note**: for local application state management (chat messages, loading state, selected provider), the project uses Angular Signals instead of Observable/BehaviorSubject. RxJS remains in use exclusively for asynchronous HTTP operations.

---

## 5. Angular Signals

| Property    | Value                                                |
|-------------|------------------------------------------------------|
| **Name**    | Angular Signals                                      |
| **Version** | Integrated in Angular 21.0.0                         |
| **Purpose** | Reactive state management without external libraries |

### Description

Angular Signals is a reactive primitive introduced in Angular 16 and became mature and stable in Angular 21. It represents the main state management mechanism in LocalMind.

### Primitives Used

| Primitive     | Description                                                      | Example                                                  |
|---------------|------------------------------------------------------------------|----------------------------------------------------------|
| \`signal()\`    | Creates a mutable reactive value                                 | \`_messages = signal<ChatMessage[]>([])\`                  |
| \`computed()\`  | Creates a derived value that updates automatically               | \`messageCount = computed(() => this._messages().length)\` |
| \`effect()\`    | Executes side effects when dependent signals change              | \`effect(() => console.log(this._messages()))\`            |
| \`.asReadonly()\`| Exposes a signal as read-only                                   | \`messages = this._messages.asReadonly()\`                 |

### State Pattern in LocalMind

The adopted pattern includes:
- **Injectable store class** (\`providedIn: 'root'\`): singleton service that encapsulates the state.
- **Private writable signals**: \`_messages = signal<ChatMessage[]>([])\` - mutable only internally.
- **Public readonly signals**: \`messages = this._messages.asReadonly()\` - exposed to components.
- **Mutation methods**: \`addMessage()\`, \`setLoading()\`, \`clearMessages()\` - single point of state modification.

### Advantages Over NgRx

| Aspect                | Angular Signals (LocalMind)    | NgRx                                  |
|-----------------------|--------------------------------|---------------------------------------|
| Boilerplate           | Zero                           | Actions, Reducers, Effects, Selectors |
| External dependencies | None                           | \`@ngrx/store\`, \`@ngrx/effects\`, etc.  |
| Type safety           | Native                         | Requires configuration                |
| Learning curve        | Low                            | High                                  |
| Suitability           | Optimal for small-medium apps  | Necessary for very complex apps       |

---

## 6. Standalone Components

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Angular Standalone Components                  |
| **Version** | Default since Angular 17+                      |
| **Purpose** | Self-contained components without NgModule     |

### Description

In Angular 21, Standalone Components are the default. LocalMind does not use NgModule: each component declares its own dependencies in the \`imports\` array of the \`@Component\` decorator.

### Advantages

- **Tree-shakable**: only the actually used components are included in the bundle.
- **Lazy loadable**: each component can be loaded on-demand via \`loadComponent\`.
- **Self-contained**: each component explicitly declares its own dependencies (pipes, directives, other components).
- **Simplicity**: elimination of NgModule complexity and redundant declarations.

### Standalone Component Example

\`\`\`typescript
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
\`\`\`

---

## 7. Angular Router

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Angular Router                                 |
| **Version** | Integrated in Angular 21.0.0                   |
| **Purpose** | Navigation and lazy loading for features       |

### Features Used

- **Lazy loading**: each feature is a separate bundle loaded on-demand via \`loadChildren\` or \`loadComponent\`.
- **\`withComponentInputBinding()\`**: route parameters are automatically mapped to component \`@Input()\` properties.
- **\`provideRouter()\`**: router configuration via function-based provider in \`app.config.ts\`.
- **Wildcard redirect**: all unrecognized routes are redirected to \`/chat\`.
- **LayoutComponent**: shell component that contains the sidebar navigation and the router-outlet.

---

## 8. SCSS

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | SCSS (Sassy CSS)                               |
| **Version** | Integrated in Angular CLI                      |
| **Purpose** | CSS preprocessor                               |

### Usage

- Global styles file: \`src/styles.scss\`.
- Each component has its own \`.scss\` file with encapsulated styles (Angular View Encapsulation).
- SCSS variables for shared themes and colors.

---

## 9. Vitest 4.0.8

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Vitest                                         |
| **Version** | 4.0.8                                          |
| **Purpose** | Test runner for unit tests and component tests |

### Rationale

Vitest replaces the traditional Karma/Jasmine stack as the project's test runner:

- **Performance**: significantly faster test execution compared to Karma thanks to Vite.
- **Native ESM**: native support for ES Modules without the need for transformation.
- **Compatible API**: API similar to Jest, facilitating migration.
- **Watch mode**: instant re-execution of modified tests.
- **Angular integration**: compatible with \`@angular/core/testing\` and component test utilities.

---

## 10. Environment Files

| File                      | Environment | Description                          |
|---------------------------|-------------|--------------------------------------|
| \`environment.ts\`          | Development | Configuration for local development  |
| \`environment.prod.ts\`     | Production  | Configuration for production         |

### environment.ts (Development)

\`\`\`typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1'
};
\`\`\`

### environment.prod.ts (Production)

\`\`\`typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1'
};
\`\`\`

> **Note**: in production, the API URL is relative (\`/api/v1\`) to allow deployment behind a reverse proxy on the same origin as the frontend.

---

## 11. Dark Mode

LocalMind supports light and dark themes via CSS custom properties and a signal-based \`ThemeService\`.

**ThemeService** (\`core/services/theme.service.ts\`):
- Signal \`currentTheme\` (\`'light'\` | \`'dark'\`) with localStorage persistence
- Default from browser's \`prefers-color-scheme\` media query
- \`effect()\` that applies the \`data-theme\` attribute on \`<html>\` and saves to localStorage
- Methods: \`toggle()\`, \`setTheme()\`, \`isDark()\` (computed)

**CSS Custom Properties** (\`styles.scss\`):
- ~40 semantic variables defined in \`:root\` (light mode) with overrides in \`[data-theme="dark"]\`
- Categories: primary colors, neutrals, status (success/error/warning/info), UI (card-bg, input-bg, hover-bg, overlay)
- All components use \`var(--color-*)\` instead of hardcoded colors

**ThemeToggleComponent** (\`shared/components/theme-toggle/\`):
- Sun/moon SVG icon with rotation animation
- Integrated in the sidebar footer (layout.component.ts)
- Supports collapsed sidebar state

---

## 12. SSE Streaming Chat

The frontend supports response streaming via SSE using \`fetch()\` + \`ReadableStream\`.

**ChatStreamService** (\`features/chat/services/chat-stream.service.ts\`):
- Uses \`fetch()\` with POST body (not EventSource, which only supports GET)
- Manual parsing of SSE lines (\`event:\` and \`data:\`)
- Returns \`Observable<SSEEvent>\` with types: \`conversation\`, \`token\`, \`metadata\`, \`done\`, \`error\`
- Automatic fallback to synchronous chat if the stream fails

**ChatStore Integration**:
- Signal \`_isStreaming\`: indicates streaming in progress
- \`addStreamingAssistantMessage()\`: adds an empty ASSISTANT message
- \`appendTokenToLastMessage(token)\`: progressively appends tokens to the last message

---

## 13. Form Validation

Template-driven validation with Angular Forms across all application forms.

**Global Styles** (\`styles.scss\`):
- \`input.ng-invalid.ng-touched\`: red border
- \`input.ng-valid.ng-touched\`: green border
- \`.form-error\`: red error text below the field
- \`.char-counter\`: character counter with \`.near-limit\` and \`.at-limit\` variants

**Validated Forms**:
| Component | Fields | Validations |
|---|---|---|
| Settings | name, baseUrl, apiKey | required, minlength, maxlength |
| Folders | path | required, minlength=2 |
| MCP Servers | name, command/url | required, minlength=2 |
| Login | password, confirmPassword | required, minlength=4, match |
| Chat | systemPrompt | maxlength=5000, char counter |

---

## 14. Internationalization (i18n)

Custom translation system based on Signals.

**TranslationService** (\`core/i18n/translation.service.ts\`):
- Signal \`currentLang\` (\`'it'\` | \`'en'\`) with localStorage persistence
- Lazy loading of JSON files from \`assets/i18n/{lang}.json\`
- Interpolation support: \`{{ 'KEY' | translate:{ param: value } }}\`

**TranslatePipe** (\`core/i18n/translate.pipe.ts\`):
- Standalone pipe for template translations
- Used across all standalone components

**LanguageSwitcherComponent** (\`shared/components/language-switcher/\`):
- IT/EN toggle integrated in the sidebar footer

---

## 15. Dependency Summary Table

| Dependency           | Version  | Purpose                                                   |
|----------------------|----------|-----------------------------------------------------------|
| Angular              | 21.0.0   | Application framework                                     |
| TypeScript           | 5.9.2    | Typed language                                            |
| RxJS                 | 7.8.0    | Asynchronous streams and HTTP                             |
| Angular Signals      | built-in | Reactive state management                                 |
| Angular Router       | built-in | Navigation and lazy loading                               |
| Angular HttpClient   | built-in | HTTP client with interceptors                             |
| Angular Forms        | built-in | Template-driven and reactive forms                        |
| SCSS                 | built-in | CSS preprocessor                                          |
| Vitest               | 4.0.8    | Test runner                                               |
| Zone.js              | built-in | Change detection (progressively replaced by Signals)      |
`;
