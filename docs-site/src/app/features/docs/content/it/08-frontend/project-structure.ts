export const content = `# Struttura Progetto Frontend

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Framework:** Angular 21.0.0

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Albero Directory](#2-albero-directory)
3. [Directory Root](#3-directory-root)
4. [Directory src/app](#4-directory-srcapp)
5. [Directory core/](#5-directory-core)
6. [Directory shared/](#6-directory-shared)
7. [Directory layout/](#7-directory-layout)
8. [Directory features/](#8-directory-features)
9. [Directory environments/](#9-directory-environments)
10. [File di Configurazione](#10-file-di-configurazione)
11. [Convenzioni](#11-convenzioni)

---

## 1. Panoramica

Il progetto frontend di LocalMind segue la struttura standard generata da Angular CLI con le seguenti personalizzazioni architetturali:

- **Standalone Components**: nessun NgModule; ogni componente dichiara le proprie dipendenze.
- **Feature-based organization**: ogni funzionalita' e' isolata in una directory dedicata sotto \`features/\`.
- **Core/Shared pattern**: servizi singleton in \`core/\`, elementi riutilizzabili in \`shared/\`.
- **Lazy loading**: ogni feature e' un bundle separato caricato on-demand.

---

## 2. Albero Directory

\`\`\`
localmind-frontend/
+-- src/
|   +-- app/
|   |   +-- app.ts                       # Root component (standalone)
|   |   +-- app.config.ts                # Application providers (router, http, animations)
|   |   +-- app.routes.ts                # Root routes con lazy loading
|   |   +-- core/
|   |   |   +-- interceptors/
|   |   |   |   +-- error.interceptor.ts  # HTTP error interceptor
|   |   |   +-- services/
|   |   |   |   +-- api.service.ts        # Base API service
|   |   |   +-- models/
|   |   |       +-- api-response.model.ts # Interfacce response generiche
|   |   +-- shared/
|   |   |   +-- pipes/
|   |   |       +-- file-size.pipe.ts     # Pipe per formattazione dimensioni file
|   |   +-- layout/
|   |   |   +-- layout.component.ts       # Shell component con sidebar e router-outlet
|   |   +-- features/
|   |       +-- chat/
|   |       |   +-- chat.routes.ts
|   |       |   +-- models/
|   |       |   +-- services/
|   |       |   +-- state/
|   |       |   +-- pages/
|   |       +-- documents/
|   |       |   +-- documents.routes.ts
|   |       |   +-- models/
|   |       |   +-- services/
|   |       |   +-- pages/
|   |       +-- search/
|   |       |   +-- search.routes.ts
|   |       |   +-- pages/
|   |       +-- folders/
|   |       |   +-- folders.routes.ts
|   |       |   +-- pages/
|   |       +-- settings/
|   |       |   +-- settings.routes.ts
|   |       |   +-- pages/
|   |       +-- dashboard/
|   |           +-- dashboard.routes.ts
|   |           +-- pages/
|   +-- environments/
|   |   +-- environment.ts                # Configurazione development
|   |   +-- environment.prod.ts           # Configurazione production
|   +-- main.ts                           # Entry point dell'applicazione
|   +-- styles.scss                       # Stili globali SCSS
+-- angular.json                          # Configurazione Angular CLI
+-- package.json                          # Dipendenze npm e script
+-- tsconfig.json                         # Configurazione TypeScript
\`\`\`

---

## 3. Directory Root

### File principali

| File             | Descrizione                                                                       |
|------------------|-----------------------------------------------------------------------------------|
| \`angular.json\`   | Configurazione Angular CLI: build target, test, output path, asset, stili globali |
| \`package.json\`   | Dipendenze npm, script (\`ng serve\`, \`ng build\`, \`ng test\`), versioni              |
| \`tsconfig.json\`  | Configurazione TypeScript: \`strict: true\`, path alias, target ES2022              |
| \`src/main.ts\`    | Entry point: \`bootstrapApplication(AppComponent, appConfig)\`                      |
| \`src/styles.scss\`| Stili globali: variabili CSS, reset, tipografia, tema                             |

### main.ts

\`\`\`typescript
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err));
\`\`\`

---

## 4. Directory src/app

### app.ts (Root Component)

Il componente radice dell'applicazione. E' un componente standalone che contiene il \`<router-outlet>\` principale.

\`\`\`typescript
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />'
})
export class AppComponent {}
\`\`\`

### app.config.ts (Application Providers)

Configura i provider a livello applicativo utilizzando la nuova API function-based di Angular:

\`\`\`typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([errorInterceptor])),
    provideAnimations()
  ]
};
\`\`\`

| Provider                        | Descrizione                                                     |
|---------------------------------|-----------------------------------------------------------------|
| \`provideRouter()\`               | Configura il router con le route definite in \`app.routes.ts\`    |
| \`withComponentInputBinding()\`   | Abilita il binding automatico dei route params sugli \`@Input()\` |
| \`provideHttpClient()\`           | Configura \`HttpClient\` con interceptor                          |
| \`withInterceptors()\`            | Registra interceptor funzionali (error interceptor)             |
| \`provideAnimations()\`           | Abilita il sistema di animazioni Angular                        |

### app.routes.ts (Root Routes)

Definisce la configurazione delle route principali con lazy loading per ogni feature. Vedere il documento \`02-routing-lazy-loading.md\` per la documentazione dettagliata.

---

## 5. Directory core/

La directory \`core/\` contiene servizi singleton, interceptor e modelli condivisi a livello applicativo. Questi elementi sono istanziati una sola volta e disponibili ovunque nell'applicazione.

### core/interceptors/error.interceptor.ts

Interceptor HTTP funzionale che gestisce gli errori delle chiamate API in modo centralizzato:

- Cattura errori HTTP (4xx, 5xx).
- Logga gli errori nella console.
- Propaga l'errore per la gestione a livello di componente.

### core/services/api.service.ts

Servizio base per le chiamate API. Fornisce:

- URL base configurato tramite environment file (\`environment.apiBaseUrl\`).
- Metodi helper per costruire URL completi.
- Integrazione con \`HttpClient\`.

### core/models/api-response.model.ts

Interfacce TypeScript per le risposte API:

- \`ErrorResponse\`: interfaccia per il formato di errore standard (\`status\`, \`message\`, \`timestamp\`, \`path\`).
- Interfacce generiche per risposte paginate (pianificate).

---

## 6. Directory shared/

La directory \`shared/\` contiene elementi riutilizzabili tra le diverse feature: pipe, direttive, componenti UI generici.

### shared/pipes/file-size.pipe.ts

Pipe standalone per la formattazione delle dimensioni file in formato leggibile:

- Input: \`number\` (byte).
- Output: \`string\` formattato (es. \`2.5 MB\`, \`512 KB\`, \`1.2 GB\`).
- Standalone: puo' essere importata direttamente nei componenti.

---

## 7. Directory layout/

### layout/layout.component.ts

Componente shell dell'applicazione che definisce la struttura visiva principale:

- **Sidebar**: navigazione laterale con link alle feature (Chat, Documents, Search, Folders, Settings, Dashboard).
- **Content area**: \`<router-outlet>\` per il rendering dei componenti feature.
- **Header** (opzionale): barra superiore con titolo e informazioni di contesto.

Il \`LayoutComponent\` e' utilizzato come componente parent nella configurazione delle route: tutte le feature sono caricate come child routes del layout.

---

## 8. Directory features/

Ogni feature e' organizzata in una directory dedicata con la seguente struttura interna:

\`\`\`
features/{feature-name}/
+-- {feature-name}.routes.ts    # Definizione route della feature
+-- models/                     # Interfacce TypeScript specifiche della feature
+-- services/                   # Servizi HTTP specifici della feature
+-- state/                      # Store basati su Signals (se necessario)
+-- pages/                      # Componenti pagina (smart components)
+-- components/                 # Componenti UI (dumb components, se necessario)
\`\`\`

### Feature implementate

| Feature     | Path        | Sotto-directory                       | Descrizione                          |
|-------------|-------------|---------------------------------------|--------------------------------------|
| Chat        | \`/chat\`     | routes, models, services, state, pages| Chat con LLM, feature piu' completa  |
| Documents   | \`/documents\`| routes, models, services, pages       | Gestione documenti                   |
| Search      | \`/search\`   | routes, pages                         | Ricerca semantica                    |
| Folders     | \`/folders\`  | routes, pages                         | Configurazione cartelle              |
| Settings    | \`/settings\` | routes, pages                         | Impostazioni LLM e API key           |
| Dashboard   | \`/dashboard\`| routes, pages                         | Health check e statistiche           |

---

## 9. Directory environments/

### environment.ts (Development)

\`\`\`typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1'
};
\`\`\`

- Utilizzato durante \`ng serve\`.
- Punta direttamente al backend Spring Boot in esecuzione sulla porta 8080.

### environment.prod.ts (Production)

\`\`\`typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1'
};
\`\`\`

- Utilizzato durante \`ng build --configuration=production\`.
- URL relativo: assume che frontend e backend siano serviti dalla stessa origin (reverse proxy).

---

## 10. File di Configurazione

### angular.json

Configurazione principale del progetto Angular CLI:

| Sezione            | Descrizione                                             |
|--------------------|---------------------------------------------------------|
| \`architect.build\`  | Configurazione build: output path, budget, optimization |
| \`architect.serve\`  | Configurazione dev server: porta 4200, proxy            |
| \`architect.test\`   | Configurazione test runner (Vitest)                     |
| \`styles\`           | Array di stili globali (\`src/styles.scss\`)              |
| \`fileReplacements\` | Sostituzione environment file per build prod            |

### tsconfig.json

| Proprieta'          | Valore    | Descrizione                              |
|---------------------|-----------|------------------------------------------|
| \`strict\`            | \`true\`    | Modalita' strict TypeScript              |
| \`target\`            | \`ES2022\`  | Target di compilazione                   |
| \`module\`            | \`ES2022\`  | Sistema di moduli                        |
| \`baseUrl\`           | \`./\`      | Base per la risoluzione dei path         |

---

## 11. Convenzioni

### Naming

| Tipo              | Convenzione                    | Esempio                            |
|-------------------|--------------------------------|------------------------------------|
| Componenti        | \`kebab-case.component.ts\`      | \`chat-page.component.ts\`           |
| Servizi           | \`kebab-case.service.ts\`        | \`chat.service.ts\`                  |
| Modelli           | \`kebab-case.model.ts\`          | \`chat-message.model.ts\`            |
| Pipe              | \`kebab-case.pipe.ts\`           | \`file-size.pipe.ts\`                |
| Interceptor       | \`kebab-case.interceptor.ts\`    | \`error.interceptor.ts\`             |
| Route             | \`kebab-case.routes.ts\`         | \`chat.routes.ts\`                   |
| Store             | \`kebab-case.store.ts\`          | \`chat.store.ts\`                    |

### Organizzazione dei componenti

- **Page components** (smart): componenti che interagiscono con i servizi e gestiscono lo stato. Risiedono in \`pages/\`.
- **UI components** (dumb): componenti presentazionali che ricevono dati tramite \`@Input()\` e emettono eventi tramite \`@Output()\`. Risiedono in \`components/\`.
- **Tutti i componenti sono standalone**: non esistono NgModule nel progetto.

### Import

Ogni componente standalone dichiara esplicitamente le proprie dipendenze nell'array \`imports\`:

\`\`\`typescript
@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, FileSizePipe],
  // ...
})
\`\`\`
`;
