import { test, expect } from '@playwright/test';
import { AuthPage } from './pages/auth.page';

test.describe('Authentication', () => {

  test.describe('Login Page', () => {
    test('should show login page when auth is configured but not authenticated', async ({ page }) => {
      // Mock auth status - configured, not authenticated
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: true, authenticated: false } })
      );

      await page.goto('/login');
      const auth = new AuthPage(page);

      await expect(auth.title).toBeVisible();
      await expect(auth.passwordInput).toBeVisible();
      await expect(auth.confirmPasswordInput).not.toBeVisible();
    });

    test('should show setup form when auth not configured', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: false, authenticated: false } })
      );

      await page.goto('/login');
      const auth = new AuthPage(page);

      await expect(auth.setupDesc).toBeVisible();
      await expect(auth.passwordInput).toBeVisible();
      await expect(auth.confirmPasswordInput).toBeVisible();
    });

    test('should login successfully with correct password', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: true, authenticated: false } })
      );
      await page.route('**/api/v1/auth/login', route =>
        route.fulfill({ json: { token: 'test-token-123', expiresIn: 86400 } })
      );
      // Mock other API calls that the app might make after login
      await page.route('**/api/v1/dashboard/**', route =>
        route.fulfill({ json: { status: 'UP' } })
      );
      await page.route('**/api/v1/**', route => {
        if (!route.request().url().includes('/auth/')) {
          route.fulfill({ json: [] });
        }
      });

      await page.goto('/login');
      const auth = new AuthPage(page);

      await auth.login('testpassword');

      // Should redirect away from login page
      await page.waitForURL(url => !url.pathname.includes('/login'));
      await expect(page).not.toHaveURL(/\/login/);
    });

    test('should show error on invalid credentials', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: true, authenticated: false } })
      );
      await page.route('**/api/v1/auth/login', route =>
        route.fulfill({ status: 401, json: { status: 401, message: 'Invalid credentials' } })
      );

      await page.goto('/login');
      const auth = new AuthPage(page);

      await auth.login('wrongpassword');

      await expect(auth.errorMsg).toBeVisible();
    });

    test('should setup password successfully', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: false, authenticated: false } })
      );
      await page.route('**/api/v1/auth/setup', route =>
        route.fulfill({ json: { token: 'new-token-456', expiresIn: 86400 } })
      );
      await page.route('**/api/v1/**', route => {
        if (!route.request().url().includes('/auth/')) {
          route.fulfill({ json: [] });
        }
      });

      await page.goto('/login');
      const auth = new AuthPage(page);

      await auth.setup('newpassword', 'newpassword');

      // Should redirect away from login page
      await page.waitForURL(url => !url.pathname.includes('/login'));
      await expect(page).not.toHaveURL(/\/login/);
    });

    test('should show error when passwords do not match', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: false, authenticated: false } })
      );

      await page.goto('/login');
      const auth = new AuthPage(page);

      await auth.setup('password1', 'password2');

      await expect(auth.errorMsg).toBeVisible();
      await expect(auth.errorMsg).toContainText(/match/i);
    });

    test('should show error when password is too short', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: false, authenticated: false } })
      );

      await page.goto('/login');
      const auth = new AuthPage(page);

      await auth.setup('abc', 'abc');

      await expect(auth.errorMsg).toBeVisible();
      await expect(auth.errorMsg).toContainText(/4 character/i);
    });
  });

  test.describe('Auth Guard', () => {
    test('should redirect to login when accessing protected route without authentication', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: true, authenticated: false } })
      );

      await page.goto('/chat');

      await expect(page).toHaveURL(/\/login/);
    });

    test('should allow access to protected routes when not configured', async ({ page }) => {
      await page.route('**/api/v1/auth/status', route =>
        route.fulfill({ json: { configured: false, authenticated: false } })
      );
      await page.route('**/api/v1/**', route => {
        if (!route.request().url().includes('/auth/')) {
          route.fulfill({ json: [] });
        }
      });

      await page.goto('/chat');

      // Should not redirect to login when auth is not configured
      await expect(page).toHaveURL(/\/chat/);
    });
  });
});
