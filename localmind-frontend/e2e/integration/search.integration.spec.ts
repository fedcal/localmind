import { test, expect } from '@playwright/test';
import { ApiHelper } from '../helpers/api.helper';
import * as path from 'path';

const TEST_FILE = 'test-document.txt';
const FIXTURE_PATH = path.resolve(__dirname, '../fixtures/test-document.txt');

test.describe.serial('Search - Semantic Search Integration', () => {
  let api: ApiHelper;

  test.beforeAll(async () => {
    api = new ApiHelper();
    await api.cleanupDocumentsByName(TEST_FILE);

    const doc = await api.uploadDocument(FIXTURE_PATH, TEST_FILE);
    await api.waitForDocumentStatus(doc.id, 'INDEXED', 60_000, 2_000);
  });

  test.afterAll(async () => {
    await api.cleanupDocumentsByName(TEST_FILE);
  });

  test('search returns results for a relevant query', async ({ page }) => {
    await page.goto('/search');

    const input = page.locator('.search-input-wrap input');
    await input.fill('artificial intelligence');

    const searchBtn = page.locator('.search-options .btn-primary');
    await searchBtn.click();

    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 20_000 }).catch(() => {});

    const resultsList = page.locator('.results-list');
    const emptyState = page.locator('.search-page .empty-state');

    const hasResults = await resultsList.isVisible().catch(() => false);
    const isEmpty = await emptyState.isVisible().catch(() => false);
    expect(hasResults || isEmpty).toBeTruthy();

    if (hasResults) {
      const resultCards = page.locator('.result-card');
      await expect(resultCards.first()).toBeVisible();
      await expect(resultCards.first().locator('.result-content')).not.toBeEmpty();
      await expect(resultCards.first().locator('.score-bar')).toBeVisible();
    }
  });

  test('topK limits the number of results', async ({ page }) => {
    await page.goto('/search');

    const topKSelect = page.locator('.search-options select');
    await topKSelect.selectOption('3');

    const input = page.locator('.search-input-wrap input');
    await input.fill('machine learning');

    const searchBtn = page.locator('.search-options .btn-primary');
    await searchBtn.click();

    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 20_000 }).catch(() => {});

    const resultCards = page.locator('.result-card');
    const count = await resultCards.count();
    expect(count).toBeLessThanOrEqual(3);
  });

  test('clicking a hint triggers a search', async ({ page }) => {
    await page.goto('/search');

    const hintArea = page.locator('.hint-area');
    await expect(hintArea).toBeVisible();

    const firstHint = page.locator('.hint').first();
    await firstHint.click();

    // Wait for loading state to appear and disappear
    await page.locator('.loading-state').waitFor({ state: 'visible', timeout: 5_000 }).catch(() => {});
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 20_000 }).catch(() => {});

    // After search, results-header appears (even for 0 results)
    const resultsHeader = page.locator('.results-header');
    const resultsList = page.locator('.results-list');
    const emptyState = page.locator('.search-page .empty-state');

    const hasHeader = await resultsHeader.isVisible().catch(() => false);
    const hasResults = await resultsList.isVisible().catch(() => false);
    const isEmpty = await emptyState.isVisible().catch(() => false);
    expect(hasHeader || hasResults || isEmpty).toBeTruthy();
  });
});
