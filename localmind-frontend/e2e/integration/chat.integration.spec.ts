import { test, expect } from '@playwright/test';

test.describe('Chat - LLM Integration', () => {
  test('send a message and receive a response or error from backend', async ({ page }) => {
    await page.goto('/chat');
    await expect(page.locator('.welcome')).toBeVisible();

    const textarea = page.locator('.input-area textarea');
    await textarea.fill('What is 2+2? Reply with just the number.');

    const sendBtn = page.locator('.send-btn');
    await sendBtn.click();

    await expect(page.locator('.message.user')).toBeVisible();

    // Wait for the typing indicator to appear and then disappear (LLM response or error)
    await page.locator('.typing-indicator').waitFor({ state: 'visible', timeout: 10_000 }).catch(() => {});
    await page.locator('.typing-indicator').waitFor({ state: 'hidden', timeout: 90_000 });

    // Backend was called: either got a response or an error
    const assistantMsg = page.locator('.message.assistant .msg-text');
    const errorBar = page.locator('.error-bar');

    const gotResponse = await assistantMsg.isVisible().catch(() => false);
    const gotError = await errorBar.isVisible().catch(() => false);
    expect(gotResponse || gotError).toBeTruthy();

    if (gotResponse) {
      const responseText = await assistantMsg.textContent();
      expect(responseText!.trim().length).toBeGreaterThan(0);
    }
  });

  test('multiple messages in a conversation', async ({ page }) => {
    await page.goto('/chat');

    const textarea = page.locator('.input-area textarea');
    const sendBtn = page.locator('.send-btn');

    // First message
    await textarea.fill('Say hello');
    await sendBtn.click();
    await page.locator('.typing-indicator').waitFor({ state: 'visible', timeout: 10_000 }).catch(() => {});
    await page.locator('.typing-indicator').waitFor({ state: 'hidden', timeout: 90_000 });

    // Check that at least the user message was added
    await expect(page.locator('.message.user')).toHaveCount(1);

    // Check backend responded (message or error)
    const gotFirstResponse = await page.locator('.message.assistant').isVisible().catch(() => false);
    const gotFirstError = await page.locator('.error-bar').isVisible().catch(() => false);
    expect(gotFirstResponse || gotFirstError).toBeTruthy();

    if (!gotFirstResponse) return; // Skip second message if first failed

    // Second message
    await textarea.fill('What did I just say?');
    await sendBtn.click();
    await page.locator('.typing-indicator').waitFor({ state: 'visible', timeout: 10_000 }).catch(() => {});
    await page.locator('.typing-indicator').waitFor({ state: 'hidden', timeout: 90_000 });

    await expect(page.locator('.message.user')).toHaveCount(2);
    await expect(page.locator('.message.assistant')).toHaveCount(2);
  });

  test('suggestion button triggers backend call', async ({ page }) => {
    await page.goto('/chat');
    await expect(page.locator('.welcome')).toBeVisible();

    const firstSuggestion = page.locator('.suggestion').first();
    await firstSuggestion.click();

    await expect(page.locator('.message.user')).toBeVisible();

    await page.locator('.typing-indicator').waitFor({ state: 'visible', timeout: 10_000 }).catch(() => {});
    await page.locator('.typing-indicator').waitFor({ state: 'hidden', timeout: 90_000 });

    const assistantMsg = page.locator('.message.assistant .msg-text');
    const errorBar = page.locator('.error-bar');

    const gotResponse = await assistantMsg.isVisible().catch(() => false);
    const gotError = await errorBar.isVisible().catch(() => false);
    expect(gotResponse || gotError).toBeTruthy();
  });
});
