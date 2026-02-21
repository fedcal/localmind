import { Page, Locator } from '@playwright/test';

export class SettingsPage {
  readonly title: Locator;
  readonly addProviderBtn: Locator;
  readonly addForm: Locator;
  readonly providerList: Locator;
  readonly providerCards: Locator;
  readonly emptyState: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.settings-page .page-header h1');
    this.addProviderBtn = page.locator('.settings-page .page-header .btn-primary');
    this.addForm = page.locator('.settings-page .add-form');
    this.providerList = page.locator('.provider-list');
    this.providerCards = page.locator('.provider-card');
    this.emptyState = page.locator('.settings-page .empty-state');
  }
}
