export const content = `# E2E Testing with Playwright

## Overview

LocalMind uses [Playwright](https://playwright.dev/) for end-to-end (E2E) testing of the Angular frontend. Tests verify the user interface by navigating real pages in the Chromium browser.

## Prerequisites

- Node.js 22+ and npm 11+
- Frontend running at \`http://localhost:4200\`
- Backend running at \`http://localhost:8080\`
- MySQL, Ollama, and Qdrant active

## Installation

\`\`\`bash
cd localmind-frontend
npm install
npx playwright install chromium
\`\`\`

## Available Commands

| Command | Description |
|---------|-------------|
| \`npm run e2e\` | Run all tests (UI + integration) in headless mode |
| \`npm run e2e:headed\` | Run all tests with the browser visible |
| \`npm run e2e:ui\` | Open Playwright's GUI to run and debug tests |
| \`npm run e2e:report\` | Open the HTML report from the last run |
| \`npm run e2e:ui-only\` | Run only UI tests (no integration) |
| \`npm run e2e:integration\` | Run only integration tests with backend |
| \`npm run e2e:integration:headed\` | Run integration tests with the browser visible |

## Running Tests

1. Start backend and frontend:
   \`\`\`bash
   ./scripts/start-all.sh
   \`\`\`

2. In another terminal, run the tests:
   \`\`\`bash
   cd localmind-frontend
   npm run e2e
   \`\`\`

3. To view the report:
   \`\`\`bash
   npm run e2e:report
   \`\`\`

## Test Structure

\`\`\`
localmind-frontend/
  playwright.config.ts          # Playwright configuration
  tsconfig.e2e.json             # TypeScript config for tests
  e2e/
    pages/                      # Page Objects
      layout.page.ts            # Sidebar and navigation
      dashboard.page.ts         # Dashboard page
      chat.page.ts              # Chat page
      documents.page.ts         # Documents page
      search.page.ts            # Search page
      folders.page.ts           # Folders page
      settings.page.ts          # Settings page
      guide.page.ts             # Guide page
    navigation.spec.ts          # Sidebar navigation tests
    dashboard.spec.ts           # Dashboard tests
    chat.spec.ts                # Chat tests
    documents.spec.ts           # Documents tests
    search.spec.ts              # Search tests
    folders.spec.ts             # Folders tests
    settings.spec.ts            # Settings tests
    guide.spec.ts               # User guide tests
    mcp.spec.ts                 # MCP tests
    fixtures/
      test-document.txt         # Test file for upload/search
    helpers/
      api.helper.ts             # HTTP helper for setup/teardown
    integration/                # Backend integration tests
      dashboard.integration.spec.ts
      settings.integration.spec.ts
      documents.integration.spec.ts
      search.integration.spec.ts
      chat.integration.spec.ts
      mcp.integration.spec.ts
\`\`\`

## Page Object Pattern

Tests use the **Page Object** pattern to encapsulate CSS selectors and common actions for each page. Each page object exposes:

- **Locators** for key page elements
- **Methods** for common actions (navigation, clicks, etc.)

Usage example:

\`\`\`typescript
import { test, expect } from '@playwright/test';
import { DashboardPage } from './pages/dashboard.page';

test('dashboard loads', async ({ page }) => {
  await page.goto('/dashboard');
  const dashboard = new DashboardPage(page);
  await expect(dashboard.title).toBeVisible();
  await expect(dashboard.statCards).toHaveCount(4);
});
\`\`\`

## Writing New Tests

1. If the page doesn't have a page object, create one in \`e2e/pages/\`
2. Create a \`.spec.ts\` file in \`e2e/\`
3. Use \`test.describe()\` to group tests
4. Use \`test.beforeEach()\` to navigate to the page
5. Use \`expect()\` for assertions

## Configuration

The \`playwright.config.ts\` file configures:

- **Browser**: Chromium only (for speed)
- **Base URL**: \`http://localhost:4200\`
- **Workers**: 1 (sequential execution, shared backend state)
- **Screenshots**: only on failure
- **Trace**: retained on failure

## Integration Tests

In addition to the UI tests that check element visibility, the project includes **integration** tests that exercise real backend API calls.

### Characteristics

- **Directory**: \`e2e/integration/\`
- **Timeout**: 120 seconds per test (LLM responses can be slow)
- **Prerequisites**: backend + frontend + MySQL + Ollama + Qdrant must all be running
- **Automatic cleanup**: each suite creates and removes its own test data via \`ApiHelper\`

### Coverage

| File | Features tested |
|------|----------------|
| \`dashboard.integration.spec.ts\` | Health check, data refresh from backend |
| \`settings.integration.spec.ts\` | Provider CRUD (create, test connection, verify details, delete) |
| \`documents.integration.spec.ts\` | Document upload, listing, deletion via modal |
| \`search.integration.spec.ts\` | Semantic search, topK, hint click |
| \`chat.integration.spec.ts\` | LLM message send, conversation, suggestions |
| \`mcp.integration.spec.ts\` | Server and tools list loading |

### Running

\`\`\`bash
# Integration tests only
npm run e2e:integration

# With visible browser
npm run e2e:integration:headed

# All tests (UI + integration)
npm run e2e
\`\`\`

## Troubleshooting

| Issue | Solution |
|-------|----------|
| All tests fail | Verify frontend and backend are running |
| Test timeouts | Increase \`timeout\` in \`playwright.config.ts\` |
| Selectors not found | Verify the component template hasn't changed |
| Browser not installed | Run \`npx playwright install chromium\` |
`;
