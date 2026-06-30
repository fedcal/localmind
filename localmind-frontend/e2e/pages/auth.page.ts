import { Page, Locator } from '@playwright/test';

export class AuthPage {
  readonly passwordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly submitBtn: Locator;
  readonly errorMsg: Locator;
  readonly title: Locator;
  readonly setupDesc: Locator;

  constructor(private page: Page) {
    this.passwordInput = page.locator('input[name="password"]');
    this.confirmPasswordInput = page.locator('input[name="confirmPassword"]');
    this.submitBtn = page.locator('button[type="submit"]');
    this.errorMsg = page.locator('.error-msg');
    this.title = page.locator('h2');
    this.setupDesc = page.locator('.setup-desc');
  }

  async fillPassword(password: string) {
    await this.passwordInput.fill(password);
  }

  async fillConfirmPassword(password: string) {
    await this.confirmPasswordInput.fill(password);
  }

  async submit() {
    await this.submitBtn.click();
  }

  async login(password: string) {
    await this.fillPassword(password);
    await this.submit();
  }

  async setup(password: string, confirmPassword: string) {
    await this.fillPassword(password);
    await this.fillConfirmPassword(confirmPassword);
    await this.submit();
  }
}
