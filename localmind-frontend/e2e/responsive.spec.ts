import { test, expect } from '@playwright/test';
import { LayoutPage } from './pages/layout.page';

test.describe('Responsive Layout', () => {
  test.beforeEach(async ({ page }) => {
    // Mock auth as not configured (no login needed)
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );
    await page.route('**/api/v1/**', route => {
      if (!route.request().url().includes('/auth/')) {
        route.fulfill({ json: [] });
      }
    });
  });

  test('should show full sidebar on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/');

    const layout = new LayoutPage(page);
    await expect(layout.sidebar).toBeVisible();
    await expect(layout.brandText).toBeVisible();
  });

  test('should show sidebar on mobile (initially may be collapsed)', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');

    const layout = new LayoutPage(page);
    // Sidebar should exist in the DOM
    await expect(layout.sidebar).toBeAttached();
  });

  test('should toggle sidebar on collapse button click', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/');

    const layout = new LayoutPage(page);

    // Initially expanded - brand text should be visible
    await expect(layout.brandText).toBeVisible();

    // Click collapse button
    await layout.collapseBtn.click();

    // Brand text should now be hidden
    await expect(layout.brandText).not.toBeVisible();

    // Click again to expand
    await layout.collapseBtn.click();

    // Brand text should be visible again
    await expect(layout.brandText).toBeVisible();
  });

  test('should maintain navigation functionality on mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');

    const layout = new LayoutPage(page);

    // Navigate to dashboard
    await layout.navigateTo('dashboard');
    await expect(page).toHaveURL(/\/dashboard/);

    // Navigate back to chat
    await layout.navigateTo('chat');
    await expect(page).toHaveURL(/\/chat/);
  });

  test('should maintain navigation functionality on tablet viewport', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto('/');

    const layout = new LayoutPage(page);

    await expect(layout.sidebar).toBeVisible();

    // Test navigation
    await layout.navigateTo('documents');
    await expect(page).toHaveURL(/\/documents/);
  });

  test('should maintain navigation functionality on large desktop viewport', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/');

    const layout = new LayoutPage(page);

    await expect(layout.sidebar).toBeVisible();
    await expect(layout.brandText).toBeVisible();

    // Test navigation
    await layout.navigateTo('settings');
    await expect(page).toHaveURL(/\/settings/);
  });
});
