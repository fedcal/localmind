import { test, expect } from '@playwright/test';

test.describe('MCP - Integration', () => {
  test('servers page loads data from backend', async ({ page }) => {
    await page.goto('/mcp/servers');
    await page.waitForLoadState('networkidle');

    const serverList = page.locator('.server-list');
    const emptyState = page.locator('.empty-state');

    const hasServers = await serverList.isVisible().catch(() => false);
    const isEmpty = await emptyState.isVisible().catch(() => false);
    expect(hasServers || isEmpty).toBeTruthy();
  });

  test('tools page loads data from backend', async ({ page }) => {
    await page.goto('/mcp/tools');
    await page.waitForLoadState('networkidle');
    await page.locator('.loading-state').waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {});

    // Tools page has local tools section and external tools section
    const toolCard = page.locator('.tool-card');
    const emptyLocal = page.locator('.section .empty').first();

    const hasTools = await toolCard.first().isVisible().catch(() => false);
    const hasEmptyMsg = await emptyLocal.isVisible().catch(() => false);
    expect(hasTools || hasEmptyMsg).toBeTruthy();
  });
});
