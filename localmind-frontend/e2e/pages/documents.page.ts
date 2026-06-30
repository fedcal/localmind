import { Page, Locator } from '@playwright/test';

export class DocumentsPage {
  readonly title: Locator;
  readonly uploadBtn: Locator;
  readonly filterTabs: Locator;
  readonly docGrid: Locator;
  readonly emptyState: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.documents-page .page-header h1');
    this.uploadBtn = page.locator('.documents-page .header-actions .btn-primary');
    this.filterTabs = page.locator('.filter-tabs button');
    this.docGrid = page.locator('.doc-grid');
    this.emptyState = page.locator('.documents-page .empty-state');
  }
}
