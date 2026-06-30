import { test, expect } from '@playwright/test';
import { ApiHelper } from '../helpers/api.helper';
import * as path from 'path';

const TEST_FILE = 'test-document.txt';
const FIXTURE_PATH = path.resolve(__dirname, '../fixtures/test-document.txt');

test.describe.serial('Documents - Upload/List/Delete Integration', () => {
  let api: ApiHelper;

  test.beforeAll(async () => {
    api = new ApiHelper();
    await api.cleanupDocumentsByName(TEST_FILE);
  });

  test.afterAll(async () => {
    await api.cleanupDocumentsByName(TEST_FILE);
  });

  test('upload a document via the UI', async ({ page }) => {
    await page.goto('/documents');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(FIXTURE_PATH);

    await expect(page.locator('.notification.success')).toBeVisible({ timeout: 30_000 });

    const docCard = page.locator('.doc-card', { has: page.locator(`.doc-name:has-text("${TEST_FILE}")`) });
    await expect(docCard).toBeVisible();
  });

  test('uploaded document appears in the document list', async ({ page }) => {
    await page.goto('/documents');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const docCard = page.locator('.doc-card', { has: page.locator(`.doc-name:has-text("${TEST_FILE}")`) });
    await expect(docCard).toBeVisible();

    const badge = docCard.locator('.badge');
    await expect(badge).toBeVisible();
  });

  test('delete the uploaded document via the UI', async ({ page }) => {
    await page.goto('/documents');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const docCard = page.locator('.doc-card', { has: page.locator(`.doc-name:has-text("${TEST_FILE}")`) });
    await expect(docCard).toBeVisible();

    await docCard.locator('.btn-icon').click();

    const modal = page.locator('.modal-overlay');
    await expect(modal).toBeVisible();
    await modal.locator('.btn-danger').click();

    await expect(page.locator('.notification.success')).toBeVisible({ timeout: 10_000 });
    await expect(page.locator(`.doc-name:has-text("${TEST_FILE}")`)).not.toBeVisible();
  });
});
