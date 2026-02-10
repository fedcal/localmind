import { Page, Locator } from '@playwright/test';

export class FoldersPage {
  readonly title: Locator;
  readonly addBtn: Locator;
  readonly addForm: Locator;
  readonly folderList: Locator;
  readonly emptyState: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.folders-page .page-header h1');
    this.addBtn = page.locator('.folders-page .page-header .btn-primary');
    this.addForm = page.locator('.folders-page .add-form');
    this.folderList = page.locator('.folder-list');
    this.emptyState = page.locator('.folders-page .empty-state');
  }
}
