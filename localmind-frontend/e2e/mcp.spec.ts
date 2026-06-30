import { test, expect } from '@playwright/test';

test.describe('MCP', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/mcp');
  });

  test('page loads with title', async ({ page }) => {
    await expect(page.locator('.mcp-dashboard h1')).toBeVisible();
  });

  test('tab navigation shows two tabs', async ({ page }) => {
    const tabs = page.locator('.tab-nav .tab');
    await expect(tabs).toHaveCount(2);
  });

  test('defaults to servers tab', async ({ page }) => {
    await expect(page).toHaveURL(/\/mcp\/servers$/);
  });

  test('clicking tools tab navigates to tools', async ({ page }) => {
    const toolsTab = page.locator('.tab-nav .tab:nth-child(2)');
    await toolsTab.click();
    await expect(page).toHaveURL(/\/mcp\/tools$/);
  });
});
