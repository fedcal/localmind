import { Page, Locator } from '@playwright/test';

export class ChatPage {
  readonly title: Locator;
  readonly providerSelect: Locator;
  readonly modelSelect: Locator;
  readonly textarea: Locator;
  readonly sendBtn: Locator;
  readonly welcomeSection: Locator;
  readonly suggestions: Locator;
  readonly messagesArea: Locator;

  constructor(private page: Page) {
    this.title = page.locator('.header-left h2');
    this.providerSelect = page.locator('.control-group:first-child select');
    this.modelSelect = page.locator('.control-group:nth-child(2) select');
    this.textarea = page.locator('.input-area textarea');
    this.sendBtn = page.locator('.send-btn');
    this.welcomeSection = page.locator('.welcome');
    this.suggestions = page.locator('.suggestion');
    this.messagesArea = page.locator('.messages-area');
  }
}
