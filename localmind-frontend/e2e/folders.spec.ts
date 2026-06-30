import { test, expect } from '@playwright/test';
import { FoldersPage } from './pages/folders.page';

test.describe('Folders', () => {
  let folders: FoldersPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/folders');
    folders = new FoldersPage(page);
  });

  test('page loads with title', async () => {
    await expect(folders.title).toBeVisible();
  });

  test('add folder button is visible', async () => {
    await expect(folders.addBtn).toBeVisible();
  });

  test('clicking add folder button shows the form', async () => {
    await expect(folders.addForm).not.toBeVisible();
    await folders.addBtn.click();
    await expect(folders.addForm).toBeVisible();
  });

  test('clicking cancel hides the form', async () => {
    await folders.addBtn.click();
    await expect(folders.addForm).toBeVisible();
    await folders.addBtn.click();
    await expect(folders.addForm).not.toBeVisible();
  });

  test('page shows folder list or empty state', async ({ page }) => {
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});
    const listVisible = await folders.folderList.isVisible().catch(() => false);
    const emptyVisible = await folders.emptyState.isVisible().catch(() => false);
    expect(listVisible || emptyVisible).toBeTruthy();
  });
});
