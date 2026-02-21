import { test, expect } from '@playwright/test';
import { NotFoundPage } from './pages/not-found.page';

test.describe('404 Not Found Page', () => {

  test('should show 404 page for unknown routes', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );

    await page.goto('/nonexistent-page-xyz');

    const notFound = new NotFoundPage(page);

    await expect(notFound.errorCode).toBeVisible();
    await expect(notFound.errorCode).toHaveText('404');
  });

  test('should display title and description on 404 page', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );

    await page.goto('/this-route-does-not-exist');

    const notFound = new NotFoundPage(page);

    await expect(notFound.title).toBeVisible();
    await expect(notFound.description).toBeVisible();
  });

  test('should show back button on 404 page', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );

    await page.goto('/invalid-route');

    const notFound = new NotFoundPage(page);

    await expect(notFound.backBtn).toBeVisible();
  });

  test('should navigate back to home when back button is clicked', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );
    await page.route('**/api/v1/**', route => {
      if (!route.request().url().includes('/auth/')) {
        route.fulfill({ json: [] });
      }
    });

    await page.goto('/some-random-page');

    const notFound = new NotFoundPage(page);

    await notFound.goBack();

    // Should redirect to home (which redirects to /chat by default)
    await expect(page).toHaveURL(/\/(chat)?$/);
  });

  test('should show 404 for route with multiple invalid segments', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );

    await page.goto('/invalid/nested/route/path');

    const notFound = new NotFoundPage(page);

    await expect(notFound.errorCode).toBeVisible();
    await expect(notFound.errorCode).toHaveText('404');
  });

  test('should show 404 for almost-valid route names', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );

    // Try routes that are close to valid routes but not exact
    await page.goto('/chats'); // instead of /chat

    const notFound = new NotFoundPage(page);

    await expect(notFound.errorCode).toBeVisible();
  });

  test('should maintain 404 page layout structure', async ({ page }) => {
    await page.route('**/api/v1/auth/status', route =>
      route.fulfill({ json: { configured: false, authenticated: false } })
    );

    await page.goto('/xyz');

    // Check the complete structure exists
    const notFoundContainer = page.locator('.not-found');
    const notFoundCard = page.locator('.not-found-card');

    await expect(notFoundContainer).toBeVisible();
    await expect(notFoundCard).toBeVisible();
  });
});
