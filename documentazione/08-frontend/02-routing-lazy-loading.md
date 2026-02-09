# Routing e Lazy Loading

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Framework:** Angular 21.0.0

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Configurazione del Router](#2-configurazione-del-router)
3. [Root Routes](#3-root-routes)
4. [Layout Component come Shell](#4-layout-component-come-shell)
5. [Lazy Loading per Feature](#5-lazy-loading-per-feature)
6. [Albero di Routing](#6-albero-di-routing)
7. [Pattern di Configurazione Feature Routes](#7-pattern-di-configurazione-feature-routes)
8. [Component Input Binding](#8-component-input-binding)
9. [Wildcard e Redirect](#9-wildcard-e-redirect)
10. [Verifica del Bundle Splitting](#10-verifica-del-bundle-splitting)

---

## 1. Panoramica

Il sistema di routing di LocalMind e' basato su Angular Router con le seguenti caratteristiche:

- **Lazy loading**: ogni feature e' caricata come bundle separato tramite `loadChildren` o `loadComponent`.
- **LayoutComponent come shell**: tutte le feature sono renderizzate all'interno di un layout comune.
- **Component Input Binding**: i parametri di route sono automaticamente mappati sugli `@Input()` dei componenti.
- **Wildcard redirect**: le rotte non riconosciute vengono reindirizzate alla chat.

---

## 2. Configurazione del Router

Il router e' configurato in `app.config.ts` tramite la funzione `provideRouter()`:

```typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    // ... altri provider
  ]
};
```

### provideRouter()

| Parametro                     | Descrizione                                         |
|-------------------------------|-----------------------------------------------------|
| `routes`                      | Array di configurazione route importato da `app.routes.ts` |
| `withComponentInputBinding()` | Feature flag che abilita il binding automatico dei route params |

---

## 3. Root Routes

Le route principali sono definite in `app.routes.ts`:

```typescript
import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: 'chat',
        loadChildren: () =>
          import('./features/chat/chat.routes').then(m => m.CHAT_ROUTES)
      },
      {
        path: 'documents',
        loadChildren: () =>
          import('./features/documents/documents.routes').then(m => m.DOCUMENTS_ROUTES)
      },
      {
        path: 'search',
        loadChildren: () =>
          import('./features/search/search.routes').then(m => m.SEARCH_ROUTES)
      },
      {
        path: 'folders',
        loadChildren: () =>
          import('./features/folders/folders.routes').then(m => m.FOLDERS_ROUTES)
      },
      {
        path: 'settings',
        loadChildren: () =>
          import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
      },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
      },
      {
        path: '',
        redirectTo: 'chat',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'chat'
  }
];
```

### Struttura delle route

| Path          | Tipo           | Target                                    | Loading       |
|---------------|----------------|-------------------------------------------|---------------|
| `''`          | Component      | `LayoutComponent` (shell)                 | Eager         |
| `'chat'`      | `loadChildren` | `features/chat/chat.routes.ts`            | Lazy          |
| `'documents'` | `loadChildren` | `features/documents/documents.routes.ts`  | Lazy          |
| `'search'`    | `loadChildren` | `features/search/search.routes.ts`        | Lazy          |
| `'folders'`   | `loadChildren` | `features/folders/folders.routes.ts`      | Lazy          |
| `'settings'`  | `loadChildren` | `features/settings/settings.routes.ts`    | Lazy          |
| `'dashboard'` | `loadChildren` | `features/dashboard/dashboard.routes.ts`  | Lazy          |
| `''` (full)   | Redirect       | `/chat`                                   | -             |
| `'**'`        | Redirect       | `/chat`                                   | -             |

---

## 4. Layout Component come Shell

Il `LayoutComponent` funge da shell dell'applicazione: contiene la struttura visiva comune (sidebar, header) e il `<router-outlet>` dove vengono renderizzate le feature.

```
+-------+-------------------------------------------+
|       |                                           |
|  S    |   <router-outlet>                         |
|  I    |                                           |
|  D    |   Feature Component                       |
|  E    |   (caricato dinamicamente)                |
|  B    |                                           |
|  A    |                                           |
|  R    |                                           |
|       |                                           |
+-------+-------------------------------------------+
```

### Funzionamento

1. L'utente accede a `http://localhost:4200/chat`.
2. Angular Router attiva la route `path: ''` e renderizza `LayoutComponent`.
3. Il router attiva la child route `path: 'chat'` e carica il bundle della feature Chat.
4. Il componente della feature Chat viene renderizzato nel `<router-outlet>` del layout.

---

## 5. Lazy Loading per Feature

Il lazy loading e' implementato tramite la funzione `loadChildren()` con import dinamico ES. Ogni feature esporta un array di route con una costante tipicamente denominata `{FEATURE}_ROUTES`.

### Meccanismo

```typescript
{
  path: 'chat',
  loadChildren: () =>
    import('./features/chat/chat.routes').then(m => m.CHAT_ROUTES)
}
```

1. **Import dinamico**: `import('./features/chat/chat.routes')` genera un chunk JavaScript separato al momento del build.
2. **Risoluzione della costante**: `.then(m => m.CHAT_ROUTES)` estrae l'array di route esportato.
3. **Caricamento on-demand**: il bundle viene scaricato solo quando l'utente naviga alla rotta corrispondente.

### Vantaggi

| Vantaggio                      | Descrizione                                         |
|--------------------------------|-----------------------------------------------------|
| **Dimensione bundle iniziale** | Solo il codice del layout e del router viene caricato all'avvio |
| **Performance percepita**      | L'utente non attende il download di feature non utilizzate |
| **Cache indipendente**         | Ogni bundle e' un file separato con il proprio hash; le modifiche a una feature non invalidano la cache delle altre |
| **Tree-shaking ottimale**      | I componenti standalone non referenziati vengono eliminati dal build |

---

## 6. Albero di Routing

```
/
+-- '' (LayoutComponent) [eager]
    +-- chat        [lazy] --> ChatPageComponent
    +-- documents   [lazy] --> DocumentListPageComponent
    +-- search      [lazy] --> SearchPageComponent
    +-- folders     [lazy] --> FolderConfigPageComponent
    +-- settings    [lazy] --> SettingsPageComponent
    +-- dashboard   [lazy] --> DashboardPageComponent
    +-- '' (redirect -> /chat)
+-- ** (redirect -> /chat)
```

### Navigazione

| URL                          | Componente renderizzato          |
|------------------------------|----------------------------------|
| `http://localhost:4200/`     | Redirect a `/chat`               |
| `http://localhost:4200/chat` | `LayoutComponent` + `ChatPageComponent` |
| `http://localhost:4200/documents` | `LayoutComponent` + `DocumentListPageComponent` |
| `http://localhost:4200/search` | `LayoutComponent` + `SearchPageComponent` |
| `http://localhost:4200/folders` | `LayoutComponent` + `FolderConfigPageComponent` |
| `http://localhost:4200/settings` | `LayoutComponent` + `SettingsPageComponent` |
| `http://localhost:4200/dashboard` | `LayoutComponent` + `DashboardPageComponent` |
| `http://localhost:4200/xyz`  | Redirect a `/chat`               |

---

## 7. Pattern di Configurazione Feature Routes

Ogni feature definisce le proprie route in un file dedicato `{feature}.routes.ts`:

### Esempio: chat.routes.ts

```typescript
import { Routes } from '@angular/router';

export const CHAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/chat-page/chat-page.component').then(m => m.ChatPageComponent)
  }
];
```

### Esempio con sotto-route: documents.routes.ts

```typescript
import { Routes } from '@angular/router';

export const DOCUMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/document-list-page/document-list-page.component')
        .then(m => m.DocumentListPageComponent)
  }
];
```

### Convenzioni

| Convenzione                     | Descrizione                                         |
|---------------------------------|-----------------------------------------------------|
| Costante `{FEATURE}_ROUTES`     | Array di route esportato come costante              |
| `loadComponent()`               | Lazy loading del singolo componente pagina           |
| `path: ''`                      | Il componente principale e' montato sulla route root della feature |

---

## 8. Component Input Binding

La funzionalita' `withComponentInputBinding()` consente il binding automatico dei parametri di route (path params, query params, data) sugli `@Input()` dei componenti.

### Esempio

```typescript
// Route configuration
{ path: 'documents/:id', loadComponent: () => ... }

// Component
@Component({ ... })
export class DocumentDetailComponent {
  @Input() id!: string;  // Automaticamente popolato con il valore di :id
}
```

### Vantaggi

- Eliminazione dell'iniezione di `ActivatedRoute` e della sottoscrizione a `paramMap`.
- Codice piu' dichiarativo e testabile.
- Compatibilita' con il sistema di Signals di Angular.

---

## 9. Wildcard e Redirect

### Redirect root

```typescript
{
  path: '',
  redirectTo: 'chat',
  pathMatch: 'full'
}
```

Reindirizza l'URL root (`/`) alla feature Chat (`/chat`). Il `pathMatch: 'full'` garantisce che il redirect avvenga solo per il path esattamente vuoto.

### Wildcard

```typescript
{
  path: '**',
  redirectTo: 'chat'
}
```

Cattura tutte le rotte non corrispondenti a nessuna configurazione e reindirizza alla Chat. Deve essere l'ultima rotta nell'array per non intercettare rotte valide.

---

## 10. Verifica del Bundle Splitting

Il lazy loading genera bundle separati verificabili nell'output di build:

```bash
ng build --configuration=production
```

Output atteso (indicativo):

```
Initial chunk files | Names         | Raw size | Estimated transfer size
main.js             | main          | 150 kB   | 45 kB
styles.css          | styles        | 12 kB    | 3 kB

Lazy chunk files    | Names         | Raw size | Estimated transfer size
chunk-CHAT.js       | chat          | 35 kB    | 10 kB
chunk-DOCUMENTS.js  | documents     | 28 kB    | 8 kB
chunk-SEARCH.js     | search        | 15 kB    | 5 kB
chunk-FOLDERS.js    | folders       | 12 kB    | 4 kB
chunk-SETTINGS.js   | settings      | 10 kB    | 3 kB
chunk-DASHBOARD.js  | dashboard     | 18 kB    | 6 kB
```

Ogni entry nella sezione "Lazy chunk files" conferma che la feature viene caricata come bundle separato.
