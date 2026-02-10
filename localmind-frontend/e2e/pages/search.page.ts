import { Page, Locator } from '@playwright/test';

export class SearchPage {
  readonly title: Locator;
  readonly searchInput: Locator;
  readonly topKSelect: Locator;
  readonly searchBtn: Locator;
  readonly hintArea: Locator;
  readonly hints: Locator;
  readonly resultsList: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.search-page .page-header h1');
    this.searchInput = page.locator('.search-input-wrap input');
    this.topKSelect = page.locator('.search-options select');
    this.searchBtn = page.locator('.search-options .btn-primary');
    this.hintArea = page.locator('.hint-area');
    this.hints = page.locator('.hint');
    this.resultsList = page.locator('.results-list');
  }
}
