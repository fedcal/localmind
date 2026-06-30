import { test, expect } from '@playwright/test';
import { ApiHelper } from '../helpers/api.helper';

const TEST_PROVIDER_NAME = 'E2E-Test-Provider';

test.describe.serial('Settings - Provider CRUD Integration', () => {
  let api: ApiHelper;

  test.beforeAll(async () => {
    api = new ApiHelper();
    await api.cleanupProvidersByName(TEST_PROVIDER_NAME);
  });

  test.afterAll(async () => {
    await api.cleanupProvidersByName(TEST_PROVIDER_NAME);
  });

  test('create a new Ollama provider via UI form', async ({ page }) => {
    await page.goto('/settings');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const addBtn = page.locator('.settings-page .page-header .btn-primary');
    await addBtn.click();

    const form = page.locator('.settings-page .add-form');
    await expect(form).toBeVisible();

    await form.locator('.form-group input[type="text"]').first().fill(TEST_PROVIDER_NAME);

    // Wait for Ollama models to load (auto-fetched on form open)
    const modelSelect = form.locator('.select-with-refresh select');
    await expect(modelSelect).toBeEnabled({ timeout: 15_000 });

    const saveBtn = form.locator('.btn.btn-primary');
    await saveBtn.click();

    await expect(page.locator('.notification.success')).toBeVisible({ timeout: 10_000 });
    await expect(page.locator(`.provider-name:has-text("${TEST_PROVIDER_NAME}")`)).toBeVisible();
  });

  test('test connection for the created provider', async ({ page }) => {
    await page.goto('/settings');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const providerCard = page.locator('.provider-card', { has: page.locator(`.provider-name:has-text("${TEST_PROVIDER_NAME}")`) });
    await expect(providerCard).toBeVisible();

    const testBtn = providerCard.locator('.provider-actions .btn-sm.btn-secondary');
    await testBtn.click();

    await expect(page.locator('.notification.success')).toBeVisible({ timeout: 15_000 });
  });

  test('provider card displays correct details', async ({ page }) => {
    await page.goto('/settings');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const providerCard = page.locator('.provider-card', { has: page.locator(`.provider-name:has-text("${TEST_PROVIDER_NAME}")`) });
    await expect(providerCard).toBeVisible();

    await expect(providerCard.locator('.provider-name')).toHaveText(TEST_PROVIDER_NAME);
    await expect(providerCard.locator('.provider-url')).toContainText('localhost:11434');
    await expect(providerCard.locator('.badge-success')).toBeVisible();
    await expect(providerCard.locator('.badge-primary')).toHaveText('OLLAMA');
  });

  test('delete the test provider', async ({ page }) => {
    await page.goto('/settings');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    const providerCard = page.locator('.provider-card', { has: page.locator(`.provider-name:has-text("${TEST_PROVIDER_NAME}")`) });
    await expect(providerCard).toBeVisible();

    const deleteBtn = providerCard.locator('.btn-sm.btn-danger');
    await deleteBtn.click();

    await expect(page.locator('.notification.success')).toBeVisible({ timeout: 10_000 });
    await expect(page.locator(`.provider-name:has-text("${TEST_PROVIDER_NAME}")`)).not.toBeVisible();
  });
});
