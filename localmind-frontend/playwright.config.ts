import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: false,
  retries: 0,
  workers: 1,
  reporter: [
    ['html', { open: 'never' }],
    ['list'],
  ],
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'ui',
      testMatch: /^(?!.*integration).*\.spec\.ts$/,
      timeout: 30_000,
      expect: { timeout: 10_000 },
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'integration',
      testDir: './e2e/integration',
      testMatch: /\.integration\.spec\.ts$/,
      timeout: 120_000,
      expect: { timeout: 30_000 },
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
