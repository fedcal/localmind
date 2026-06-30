export const content = `# Routing and Lazy Loading

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Framework:** Angular 21.0.0

---

## Table of Contents

- [Routing and Lazy Loading](#routing-and-lazy-loading)
  - [Table of Contents](#table-of-contents)
  - [1. Overview](#1-overview)
  - [2. Router Configuration](#2-router-configuration)
    - [provideRouter()](#providerouter)
  - [3. Root Routes](#3-root-routes)
    - [Route structure](#route-structure)
  - [4. Layout Component as Shell](#4-layout-component-as-shell)
    - [How it works](#how-it-works)
  - [5. Lazy Loading per Feature](#5-lazy-loading-per-feature)
    - [Mechanism](#mechanism)
    - [Advantages](#advantages)
  - [6. Routing Tree](#6-routing-tree)
    - [Navigation](#navigation)
  - [7. Feature Routes Configuration Pattern](#7-feature-routes-configuration-pattern)
    - [Example: chat.routes.ts](#example-chatroutests)
    - [Example with sub-routes: documents.routes.ts](#example-with-sub-routes-documentsroutests)
    - [Conventions](#conventions)
  - [8. Component Input Binding](#8-component-input-binding)
    - [Example](#example)
    - [Advantages](#advantages-1)
  - [9. Wildcard and Redirect](#9-wildcard-and-redirect)
    - [Root redirect](#root-redirect)
    - [Wildcard](#wildcard)
  - [10. Bundle Splitting Verification](#10-bundle-splitting-verification)

---

## 1. Overview

The LocalMind routing system is based on Angular Router with the following characteristics:

- **Lazy loading**: each feature is loaded as a separate bundle via \`loadChildren\` or \`loadComponent\`.
- **LayoutComponent as shell**: all features are rendered within a common layout.
- **Component Input Binding**: route parameters are automatically mapped to component \`@Input()\` properties.
- **Wildcard redirect**: unrecognized routes are redirected to chat.

---

## 2. Router Configuration

The router is configured in \`app.config.ts\` via the \`provideRouter()\` function:

\`\`\`typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    // ... other providers
  ]
};
\`\`\`

### provideRouter()

| Parameter                     | Description                                                       |
|-------------------------------|-------------------------------------------------------------------|
| \`routes\`                      | Route configuration array imported from \`app.routes.ts\`           |
| \`withComponentInputBinding()\` | Feature flag that enables automatic binding of route params       |

---

## 3. Root Routes

The main routes are defined in \`app.routes.ts\`:

\`\`\`typescript
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
\`\`\`

### Route structure

| Path          | Type           | Target                                    | Loading       |
|---------------|----------------|-------------------------------------------|---------------|
| \`''\`          | Component      | \`LayoutComponent\` (shell)                 | Eager         |
| \`'chat'\`      | \`loadChildren\` | \`features/chat/chat.routes.ts\`            | Lazy          |
| \`'documents'\` | \`loadChildren\` | \`features/documents/documents.routes.ts\`  | Lazy          |
| \`'search'\`    | \`loadChildren\` | \`features/search/search.routes.ts\`        | Lazy          |
| \`'folders'\`   | \`loadChildren\` | \`features/folders/folders.routes.ts\`      | Lazy          |
| \`'settings'\`  | \`loadChildren\` | \`features/settings/settings.routes.ts\`    | Lazy          |
| \`'dashboard'\` | \`loadChildren\` | \`features/dashboard/dashboard.routes.ts\`  | Lazy          |
| \`''\` (full)   | Redirect       | \`/chat\`                                   | -             |
| \`'**'\`        | Redirect       | \`/chat\`                                   | -             |

---

## 4. Layout Component as Shell

The \`LayoutComponent\` acts as the application shell: it contains the common visual structure (sidebar, header) and the \`<router-outlet>\` where features are rendered.

\`\`\`
+-------+-------------------------------------------+
|       |                                           |
|  S    |   <router-outlet>                         |
|  I    |                                           |
|  D    |   Feature Component                       |
|  E    |   (dynamically loaded)                    |
|  B    |                                           |
|  A    |                                           |
|  R    |                                           |
|       |                                           |
+-------+-------------------------------------------+
\`\`\`

### How it works

1. The user navigates to \`http://localhost:4200/chat\`.
2. Angular Router activates the \`path: ''\` route and renders \`LayoutComponent\`.
3. The router activates the child route \`path: 'chat'\` and loads the Chat feature bundle.
4. The Chat feature component is rendered in the layout's \`<router-outlet>\`.

---

## 5. Lazy Loading per Feature

Lazy loading is implemented via the \`loadChildren()\` function with dynamic ES imports. Each feature exports a route array with a constant typically named \`{FEATURE}_ROUTES\`.

### Mechanism

\`\`\`typescript
{
  path: 'chat',
  loadChildren: () =>
    import('./features/chat/chat.routes').then(m => m.CHAT_ROUTES)
}
\`\`\`

1. **Dynamic import**: \`import('./features/chat/chat.routes')\` generates a separate JavaScript chunk at build time.
2. **Constant resolution**: \`.then(m => m.CHAT_ROUTES)\` extracts the exported route array.
3. **On-demand loading**: the bundle is downloaded only when the user navigates to the corresponding route.

### Advantages

| Advantage                      | Description                                                                                                         |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------|
| **Initial bundle size**        | Only the layout and router code is loaded at startup                                                                |
| **Perceived performance**      | The user does not wait for unused features to download                                                              |
| **Independent caching**        | Each bundle is a separate file with its own hash; changes to one feature do not invalidate the cache of the others  |
| **Optimal tree-shaking**       | Unreferenced standalone components are eliminated from the build                                                    |

---

## 6. Routing Tree

\`\`\`
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
\`\`\`

### Navigation

| URL                               | Rendered component                              |
|-----------------------------------|-------------------------------------------------|
| \`http://localhost:4200/\`          | Redirect to \`/chat\`                             |
| \`http://localhost:4200/chat\`      | \`LayoutComponent\` + \`ChatPageComponent\`         |
| \`http://localhost:4200/documents\` | \`LayoutComponent\` + \`DocumentListPageComponent\` |
| \`http://localhost:4200/search\`    | \`LayoutComponent\` + \`SearchPageComponent\`       |
| \`http://localhost:4200/folders\`   | \`LayoutComponent\` + \`FolderConfigPageComponent\` |
| \`http://localhost:4200/settings\`  | \`LayoutComponent\` + \`SettingsPageComponent\`     |
| \`http://localhost:4200/dashboard\` | \`LayoutComponent\` + \`DashboardPageComponent\`    |
| \`http://localhost:4200/xyz\`       | Redirect to \`/chat\`                             |

---

## 7. Feature Routes Configuration Pattern

Each feature defines its own routes in a dedicated \`{feature}.routes.ts\` file:

### Example: chat.routes.ts

\`\`\`typescript
import { Routes } from '@angular/router';

export const CHAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/chat-page/chat-page.component').then(m => m.ChatPageComponent)
  }
];
\`\`\`

### Example with sub-routes: documents.routes.ts

\`\`\`typescript
import { Routes } from '@angular/router';

export const DOCUMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/document-list-page/document-list-page.component')
        .then(m => m.DocumentListPageComponent)
  }
];
\`\`\`

### Conventions

| Convention                      | Description                                                        |
|---------------------------------|--------------------------------------------------------------------|
| \`{FEATURE}_ROUTES\` constant     | Route array exported as a constant                                 |
| \`loadComponent()\`               | Lazy loading of the individual page component                      |
| \`path: ''\`                      | The main component is mounted on the feature's root route          |

---

## 8. Component Input Binding

The \`withComponentInputBinding()\` feature enables automatic binding of route parameters (path params, query params, data) to component \`@Input()\` properties.

### Example

\`\`\`typescript
// Route configuration
{ path: 'documents/:id', loadComponent: () => ... }

// Component
@Component({ ... })
export class DocumentDetailComponent {
  @Input() id!: string;  // Automatically populated with the :id value
}
\`\`\`

### Advantages

- Eliminates the need to inject \`ActivatedRoute\` and subscribe to \`paramMap\`.
- More declarative and testable code.
- Compatible with the Angular Signals system.

---

## 9. Wildcard and Redirect

### Root redirect

\`\`\`typescript
{
  path: '',
  redirectTo: 'chat',
  pathMatch: 'full'
}
\`\`\`

Redirects the root URL (\`/\`) to the Chat feature (\`/chat\`). The \`pathMatch: 'full'\` ensures the redirect only occurs for the exactly empty path.

### Wildcard

\`\`\`typescript
{
  path: '**',
  redirectTo: 'chat'
}
\`\`\`

Catches all routes that do not match any configuration and redirects to Chat. It must be the last route in the array to avoid intercepting valid routes.

---

## 10. Bundle Splitting Verification

Lazy loading generates separate bundles that can be verified in the build output:

\`\`\`bash
ng build --configuration=production
\`\`\`

Expected output (indicative):

\`\`\`
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
\`\`\`

Each entry in the "Lazy chunk files" section confirms that the feature is loaded as a separate bundle.
`;
