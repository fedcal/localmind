# Test E2E con Playwright

## Panoramica

LocalMind utilizza [Playwright](https://playwright.dev/) per i test end-to-end (E2E) del frontend Angular. I test verificano il funzionamento dell'interfaccia utente navigando le pagine reali nel browser Chromium.

## Prerequisiti

- Node.js 22+ e npm 11+
- Frontend in esecuzione su `http://localhost:4200`
- Backend in esecuzione su `http://localhost:8080`
- MySQL, Ollama e Qdrant attivi

## Installazione

```bash
cd localmind-frontend
npm install
npx playwright install chromium
```

## Comandi disponibili

| Comando | Descrizione |
|---------|-------------|
| `npm run e2e` | Esegue tutti i test (UI + integrazione) in modalita' headless |
| `npm run e2e:headed` | Esegue tutti i test con il browser visibile |
| `npm run e2e:ui` | Apre l'interfaccia grafica di Playwright per eseguire e debuggare i test |
| `npm run e2e:report` | Apre il report HTML dell'ultima esecuzione |
| `npm run e2e:ui-only` | Esegue solo i test UI (senza integrazione) |
| `npm run e2e:integration` | Esegue solo i test di integrazione con il backend |
| `npm run e2e:integration:headed` | Esegue i test di integrazione con il browser visibile |

## Esecuzione

1. Avviare backend e frontend:
   ```bash
   ./scripts/start-all.sh
   ```

2. In un altro terminale, eseguire i test:
   ```bash
   cd localmind-frontend
   npm run e2e
   ```

3. Per visualizzare il report:
   ```bash
   npm run e2e:report
   ```

## Struttura dei test

```
localmind-frontend/
  playwright.config.ts          # Configurazione Playwright
  tsconfig.e2e.json             # TypeScript config per i test
  e2e/
    pages/                      # Page Objects
      layout.page.ts            # Sidebar e navigazione
      dashboard.page.ts         # Pagina dashboard
      chat.page.ts              # Pagina chat
      documents.page.ts         # Pagina documenti
      search.page.ts            # Pagina ricerca
      folders.page.ts           # Pagina cartelle
      settings.page.ts          # Pagina impostazioni
      guide.page.ts             # Pagina guida
    navigation.spec.ts          # Test navigazione sidebar
    dashboard.spec.ts           # Test dashboard
    chat.spec.ts                # Test chat
    documents.spec.ts           # Test documenti
    search.spec.ts              # Test ricerca
    folders.spec.ts             # Test cartelle
    settings.spec.ts            # Test impostazioni
    guide.spec.ts               # Test guida utente
    mcp.spec.ts                 # Test MCP
    fixtures/
      test-document.txt         # File di test per upload/ricerca
    helpers/
      api.helper.ts             # Helper HTTP per setup/teardown
    integration/                # Test di integrazione con il backend
      dashboard.integration.spec.ts
      settings.integration.spec.ts
      documents.integration.spec.ts
      search.integration.spec.ts
      chat.integration.spec.ts
      mcp.integration.spec.ts
```

## Page Object Pattern

I test utilizzano il pattern **Page Object** per incapsulare i selettori CSS e le azioni comuni di ogni pagina. Ogni page object espone:

- **Locator** per gli elementi chiave della pagina
- **Metodi** per le azioni comuni (navigazione, click, etc.)

Esempio di utilizzo:

```typescript
import { test, expect } from '@playwright/test';
import { DashboardPage } from './pages/dashboard.page';

test('dashboard loads', async ({ page }) => {
  await page.goto('/dashboard');
  const dashboard = new DashboardPage(page);
  await expect(dashboard.title).toBeVisible();
  await expect(dashboard.statCards).toHaveCount(4);
});
```

## Scrivere nuovi test

1. Se la pagina non ha un page object, crearne uno in `e2e/pages/`
2. Creare il file `.spec.ts` in `e2e/`
3. Usare `test.describe()` per raggruppare i test
4. Usare `test.beforeEach()` per navigare alla pagina
5. Usare `expect()` per le asserzioni

## Configurazione

Il file `playwright.config.ts` configura:

- **Browser**: solo Chromium (per velocita')
- **Base URL**: `http://localhost:4200`
- **Workers**: 1 (esecuzione sequenziale, stato backend condiviso)
- **Screenshot**: solo in caso di fallimento
- **Trace**: mantenuta in caso di fallimento

## Test di integrazione

Oltre ai test UI che verificano la presenza degli elementi, il progetto include test di **integrazione** che esercitano le chiamate reali al backend.

### Caratteristiche

- **Directory**: `e2e/integration/`
- **Timeout**: 120 secondi per test (le risposte LLM possono essere lente)
- **Prerequisiti**: backend + frontend + MySQL + Ollama + Qdrant tutti attivi
- **Cleanup automatico**: ogni suite crea e rimuove i propri dati di test tramite `ApiHelper`

### Copertura

| File | Funzionalita' testate |
|------|----------------------|
| `dashboard.integration.spec.ts` | Health check, refresh dati dal backend |
| `settings.integration.spec.ts` | CRUD provider (crea, testa connessione, verifica dettagli, elimina) |
| `documents.integration.spec.ts` | Upload documento, lista, eliminazione via modal |
| `search.integration.spec.ts` | Ricerca semantica, topK, click hint |
| `chat.integration.spec.ts` | Invio messaggio LLM, conversazione, suggestion |
| `mcp.integration.spec.ts` | Caricamento lista server e tools |

### Esecuzione

```bash
# Solo test di integrazione
npm run e2e:integration

# Con browser visibile
npm run e2e:integration:headed

# Tutti i test (UI + integrazione)
npm run e2e
```

## Troubleshooting

| Problema | Soluzione |
|----------|----------|
| Test falliscono tutti | Verificare che frontend e backend siano attivi |
| Timeout sui test | Aumentare `timeout` in `playwright.config.ts` |
| Selettori non trovati | Verificare che il template del componente non sia cambiato |
| Browser non installato | Eseguire `npx playwright install chromium` |
