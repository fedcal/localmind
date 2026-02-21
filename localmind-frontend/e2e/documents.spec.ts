import { test, expect } from '@playwright/test';
import { DocumentsPage } from './pages/documents.page';

test.describe('Documents', () => {
  let docs: DocumentsPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/documents');
    docs = new DocumentsPage(page);
  });

  test('page loads with title', async () => {
    await expect(docs.title).toBeVisible();
  });

  test('upload button is visible', async () => {
    await expect(docs.uploadBtn).toBeVisible();
  });

  test('filter tabs are displayed', async () => {
    await expect(docs.filterTabs).toHaveCount(5);
  });

  test('All filter tab is active by default', async () => {
    await expect(docs.filterTabs.first()).toHaveClass(/active/);
  });

  test('page shows document list or empty state', async ({ page }) => {
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});
    const docGridVisible = await docs.docGrid.isVisible().catch(() => false);
    const emptyVisible = await docs.emptyState.isVisible().catch(() => false);
    expect(docGridVisible || emptyVisible).toBeTruthy();
  });
});
