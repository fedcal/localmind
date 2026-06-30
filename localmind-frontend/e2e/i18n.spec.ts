import { test, expect } from '@playwright/test';
import { LayoutPage } from './pages/layout.page';

test.describe('Internationalization', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );
    await page.route('**/api/v1/**', route => {
      if (!route.request().url().includes('/auth/')) {
        route.fulfill({ json: [] });
      }
    });
  });

  test('should display navigation elements on page load', async ({ page }) => {
    await page.goto('/');

    const layout = new LayoutPage(page);

    // Check that navigation labels are present
    const navLinks = page.locator('.nav-links a');
    await expect(navLinks.first()).toBeVisible();
    await expect(layout.brandText).toHaveText('LocalMind');
  });

  test('should display language switcher component', async ({ page }) => {
    await page.goto('/');

    // Find language switcher and verify it exists
    const langSwitcher = page.locator('app-language-switcher');
    await expect(langSwitcher).toBeVisible();
  });

  test('should show IT and EN language buttons', async ({ page }) => {
    await page.goto('/');

    const itBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'IT' });
    const enBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'EN' });

    await expect(itBtn).toBeVisible();
    await expect(enBtn).toBeVisible();
  });

  test('should have one language button active by default', async ({ page }) => {
    await page.goto('/');

    const activeLangBtn = page.locator('app-language-switcher .lang-btn.active');
    await expect(activeLangBtn).toHaveCount(1);
  });

  test('should switch language when language button is clicked', async ({ page }) => {
    await page.goto('/');

    const itBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'IT' });
    const enBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'EN' });

    // Click English button
    await enBtn.click();
    await expect(enBtn).toHaveClass(/active/);

    // Click Italian button
    await itBtn.click();
    await expect(itBtn).toHaveClass(/active/);
  });

  test('should persist active state when switching languages', async ({ page }) => {
    await page.goto('/');

    const itBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'IT' });
    const enBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'EN' });

    // Switch to English
    await enBtn.click();
    await expect(enBtn).toHaveClass(/active/);
    await expect(itBtn).not.toHaveClass(/active/);

    // Switch to Italian
    await itBtn.click();
    await expect(itBtn).toHaveClass(/active/);
    await expect(enBtn).not.toHaveClass(/active/);
  });

  test('should maintain language selection across navigation', async ({ page }) => {
    await page.goto('/');

    const layout = new LayoutPage(page);
    const enBtn = page.locator('app-language-switcher .lang-btn', { hasText: 'EN' });

    // Switch to English
    await enBtn.click();
    await expect(enBtn).toHaveClass(/active/);

    // Navigate to different pages
    await layout.navigateTo('dashboard');
    await expect(enBtn).toHaveClass(/active/);

    await layout.navigateTo('settings');
    await expect(enBtn).toHaveClass(/active/);
  });

  test('should show language switcher in collapsed sidebar', async ({ page }) => {
    await page.goto('/');

    const layout = new LayoutPage(page);
    const langSwitcher = page.locator('app-language-switcher');

    // Collapse sidebar
    await layout.collapseBtn.click();

    // Language switcher should still be visible
    await expect(langSwitcher).toBeVisible();
  });
});
