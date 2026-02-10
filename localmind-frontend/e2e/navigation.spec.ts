import { test, expect } from '@playwright/test';
import { LayoutPage } from './pages/layout.page';

test.describe('Navigation', () => {
  let layout: LayoutPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    layout = new LayoutPage(page);
  });

  test('default route redirects to /chat', async ({ page }) => {
    await expect(page).toHaveURL(/\/chat$/);
  });

  test('sidebar displays all navigation links', async () => {
    await expect(layout.sidebar).toBeVisible();
    await expect(layout.brandLogo).toBeVisible();
    await expect(layout.brandText).toHaveText('LocalMind');

    const routes = ['dashboard', 'chat', 'documents', 'search', 'folders', 'mcp', 'settings', 'guide'];
    for (const route of routes) {
      await expect(layout.navLinks[route]).toBeVisible();
    }
  });

  test('clicking sidebar links navigates to correct pages', async ({ page }) => {
    const targets = [
      { link: 'dashboard', url: /\/dashboard/ },
      { link: 'documents', url: /\/documents/ },
      { link: 'search', url: /\/search/ },
      { link: 'folders', url: /\/folders/ },
      { link: 'settings', url: /\/settings/ },
      { link: 'guide', url: /\/guide/ },
      { link: 'mcp', url: /\/mcp/ },
      { link: 'chat', url: /\/chat/ },
    ];

    for (const target of targets) {
      await layout.navigateTo(target.link as any);
      await expect(page).toHaveURL(target.url);
    }
  });

  test('sidebar collapse toggle works', async () => {
    await expect(layout.brandText).toBeVisible();
    await layout.collapseBtn.click();
    await expect(layout.brandText).not.toBeVisible();
    await layout.collapseBtn.click();
    await expect(layout.brandText).toBeVisible();
  });

  test('active link is highlighted', async ({ page }) => {
    await layout.navigateTo('dashboard');
    await expect(layout.navLinks.dashboard).toHaveClass(/active/);
    const sidebarChatLink = page.locator('nav.sidebar a[routerLink="/chat"]');
    await expect(sidebarChatLink).not.toHaveClass(/active/);
  });

  test('wildcard route redirects to /chat', async ({ page }) => {
    await page.goto('/nonexistent-page');
    await expect(page).toHaveURL(/\/chat$/);
  });
});
