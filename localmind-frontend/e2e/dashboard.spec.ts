import { test, expect } from '@playwright/test';
import { DashboardPage } from './pages/dashboard.page';

test.describe('Dashboard', () => {
  let dashboard: DashboardPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/dashboard');
    dashboard = new DashboardPage(page);
  });

  test('page loads with title', async () => {
    await expect(dashboard.title).toBeVisible();
  });

  test('displays four stat cards', async () => {
    await expect(dashboard.statCards).toHaveCount(4);
  });

  test('API status card shows UP or DOWN', async () => {
    await expect(dashboard.apiStatusValue).not.toHaveText('...');
    const text = await dashboard.apiStatusValue.textContent();
    expect(['UP', 'DOWN']).toContain(text?.trim());
  });

  test('refresh button is visible and clickable', async () => {
    await expect(dashboard.refreshBtn).toBeVisible();
    await dashboard.refreshBtn.click();
    await expect(dashboard.apiStatusValue).not.toHaveText('...');
  });

  test('quick actions section displays four action cards', async () => {
    await expect(dashboard.quickActions).toBeVisible();
    await expect(dashboard.actionCards).toHaveCount(4);
  });

  test('quick action card navigates to correct route', async ({ page }) => {
    await dashboard.actionCards.first().click();
    await expect(page).toHaveURL(/\/chat$/);
  });
});
