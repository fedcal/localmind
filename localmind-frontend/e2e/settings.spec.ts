import { test, expect } from '@playwright/test';
import { SettingsPage } from './pages/settings.page';

test.describe('Settings', () => {
  let settings: SettingsPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/settings');
    settings = new SettingsPage(page);
  });

  test('page loads with title', async () => {
    await expect(settings.title).toBeVisible();
  });

  test('add provider button is visible', async () => {
    await expect(settings.addProviderBtn).toBeVisible();
  });

  test('clicking add provider shows the form', async () => {
    await expect(settings.addForm).not.toBeVisible();
    await settings.addProviderBtn.click();
    await expect(settings.addForm).toBeVisible();
  });

  test('provider form has type selector with all providers', async ({ page }) => {
    await settings.addProviderBtn.click();
    const typeSelect = page.locator('.add-form .form-group select').first();
    const options = typeSelect.locator('option');
    await expect(options).toHaveCount(4);
  });

  test('page shows provider list or empty state after loading', async ({ page }) => {
    await page.waitForTimeout(2000);
    const listVisible = await settings.providerList.isVisible().catch(() => false);
    const emptyVisible = await settings.emptyState.isVisible().catch(() => false);
    expect(listVisible || emptyVisible).toBeTruthy();
  });
});
