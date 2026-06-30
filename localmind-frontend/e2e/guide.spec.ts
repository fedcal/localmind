import { test, expect } from '@playwright/test';
import { GuidePage } from './pages/guide.page';

test.describe('Guide', () => {
  let guide: GuidePage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/guide');
    guide = new GuidePage(page);
  });

  test('page loads with title', async () => {
    await expect(guide.title).toBeVisible();
  });

  test('table of contents is visible with 8 items', async () => {
    await expect(guide.toc).toBeVisible();
    await expect(guide.tocItems).toHaveCount(8);
  });

  test('all guide sections are rendered', async () => {
    await expect(guide.sections).toHaveCount(8);
  });

  test('clicking a section header toggles its content', async ({ page }) => {
    const secondHeader = guide.sectionHeaders.nth(1);
    await secondHeader.click();
    const secondBody = guide.sections.nth(1).locator('.section-body');
    await expect(secondBody).toBeVisible();
  });

  test('TOC item click opens corresponding section', async ({ page }) => {
    const thirdTocItem = guide.tocItems.nth(2);
    await thirdTocItem.click();
    const thirdBody = guide.sections.nth(2).locator('.section-body');
    await expect(thirdBody).toBeVisible();
  });
});
