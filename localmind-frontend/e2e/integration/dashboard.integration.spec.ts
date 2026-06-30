import { test, expect } from '@playwright/test';

test.describe('Dashboard - Integration', () => {
  test('backend health check returns UP and stat card displays it', async ({ page }) => {
    await page.goto('/dashboard');
    const apiStatValue = page.locator('.stat-card:first-child .stat-value');
    await expect(apiStatValue).not.toHaveText('...');
    await expect(apiStatValue).toHaveText('UP');
    await expect(apiStatValue).toHaveClass(/success/);
  });

  test('refresh button updates live data from backend', async ({ page }) => {
    await page.goto('/dashboard');
    const apiStatValue = page.locator('.stat-card:first-child .stat-value');
    await expect(apiStatValue).not.toHaveText('...');

    const refreshBtn = page.locator('.dashboard .page-header button');
    await refreshBtn.click();

    const docCountValue = page.locator('.stat-card:nth-child(2) .stat-value');
    await expect(docCountValue).not.toHaveText('...');
    const text = await docCountValue.textContent();
    expect(Number(text)).toBeGreaterThanOrEqual(0);
  });
});
