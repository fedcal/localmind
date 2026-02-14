import { Page, Locator } from '@playwright/test';

export class NotFoundPage {
  readonly errorCode: Locator;
  readonly title: Locator;
  readonly description: Locator;
  readonly backBtn: Locator;

  constructor(private page: Page) {
    this.errorCode = page.locator('.error-code');
    this.title = page.locator('.not-found h1');
    this.description = page.locator('.not-found p');
    this.backBtn = page.locator('.back-btn');
  }

  async goBack() {
    await this.backBtn.click();
  }
}
