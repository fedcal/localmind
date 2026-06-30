import { Page, Locator } from '@playwright/test';

export class GuidePage {
  readonly title: Locator;
  readonly toc: Locator;
  readonly tocItems: Locator;
  readonly sections: Locator;
  readonly sectionHeaders: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.guide-page .page-header h1');
    this.toc = page.locator('.toc');
    this.tocItems = page.locator('.toc-item');
    this.sections = page.locator('.guide-section');
    this.sectionHeaders = page.locator('.section-header');
  }
}
