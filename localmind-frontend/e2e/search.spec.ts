import { test, expect } from '@playwright/test';
import { SearchPage } from './pages/search.page';

test.describe('Search', () => {
  let search: SearchPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/search');
    search = new SearchPage(page);
  });

  test('page loads with title', async () => {
    await expect(search.title).toBeVisible();
  });

  test('search input is visible and enabled', async () => {
    await expect(search.searchInput).toBeVisible();
    await expect(search.searchInput).toBeEnabled();
  });

  test('topK selector defaults to 5', async () => {
    await expect(search.topKSelect).toHaveValue('5');
  });

  test('topK selector has expected options', async () => {
    const options = search.topKSelect.locator('option');
    await expect(options).toHaveCount(4);
  });

  test('search button is disabled when input is empty', async () => {
    await expect(search.searchBtn).toBeDisabled();
  });

  test('search button enables when input has text', async () => {
    await search.searchInput.fill('test query');
    await expect(search.searchBtn).toBeEnabled();
  });

  test('hint area is visible with suggestions', async () => {
    await expect(search.hintArea).toBeVisible();
    await expect(search.hints).toHaveCount(3);
  });
});
