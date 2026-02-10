import { Page, Locator } from '@playwright/test';

export class DashboardPage {
  readonly title: Locator;
  readonly refreshBtn: Locator;
  readonly statCards: Locator;
  readonly apiStatusValue: Locator;
  readonly quickActions: Locator;
  readonly actionCards: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.dashboard .page-header h1');
    this.refreshBtn = page.locator('.dashboard .page-header button');
    this.statCards = page.locator('.stat-card');
    this.apiStatusValue = page.locator('.stat-card:first-child .stat-value');
    this.quickActions = page.locator('.quick-actions');
    this.actionCards = page.locator('.action-card');
  }
}
