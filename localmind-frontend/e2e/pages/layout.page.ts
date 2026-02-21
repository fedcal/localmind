import { Page, Locator } from '@playwright/test';

export class LayoutPage {
  readonly sidebar: Locator;
  readonly brandLogo: Locator;
  readonly brandText: Locator;
  readonly collapseBtn: Locator;
  readonly version: Locator;
  readonly navLinks: Record<string, Locator>;

  constructor(private page: Page) {
    this.sidebar = page.locator('nav.sidebar');
    this.brandLogo = page.locator('.brand-logo');
    this.brandText = page.locator('.brand-text');
    this.collapseBtn = page.locator('button.collapse-btn');
    this.version = page.locator('.version');

    this.navLinks = {
      dashboard: page.locator('nav.sidebar a[routerLink="/dashboard"]'),
      chat: page.locator('nav.sidebar a[routerLink="/chat"]'),
      documents: page.locator('nav.sidebar a[routerLink="/documents"]'),
      search: page.locator('nav.sidebar a[routerLink="/search"]'),
      folders: page.locator('nav.sidebar a[routerLink="/folders"]'),
      mcp: page.locator('nav.sidebar a[routerLink="/mcp"]'),
      settings: page.locator('nav.sidebar a[routerLink="/settings"]'),
      guide: page.locator('nav.sidebar a[routerLink="/guide"]'),
    };
  }

  async navigateTo(section: keyof typeof this.navLinks) {
    await this.navLinks[section].click();
  }
}
