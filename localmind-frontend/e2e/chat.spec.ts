import { test, expect } from '@playwright/test';
import { ChatPage } from './pages/chat.page';

test.describe('Chat', () => {
  let chat: ChatPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/chat');
    chat = new ChatPage(page);
  });

  test('page loads with title and welcome section', async () => {
    await expect(chat.title).toBeVisible();
    await expect(chat.welcomeSection).toBeVisible();
  });

  test('provider selector is visible with options', async () => {
    await expect(chat.providerSelect).toBeVisible();
    const options = chat.providerSelect.locator('option');
    const count = await options.count();
    expect(count).toBeGreaterThanOrEqual(2);
  });

  test('model selector is visible with options', async () => {
    await expect(chat.modelSelect).toBeVisible();
    const options = chat.modelSelect.locator('option');
    const count = await options.count();
    expect(count).toBeGreaterThan(0);
  });

  test('chat textarea is enabled and accepts input', async () => {
    await expect(chat.textarea).toBeVisible();
    await expect(chat.textarea).toBeEnabled();
    await chat.textarea.fill('Test message');
    await expect(chat.textarea).toHaveValue('Test message');
  });

  test('send button is disabled when textarea is empty', async () => {
    await expect(chat.sendBtn).toBeDisabled();
  });

  test('send button is enabled when textarea has content', async () => {
    await chat.textarea.fill('Hello');
    await expect(chat.sendBtn).toBeEnabled();
  });

  test('suggestion buttons are visible in welcome state', async () => {
    await expect(chat.suggestions).toHaveCount(3);
  });
});
