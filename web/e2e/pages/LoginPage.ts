import {Page} from '@playwright/test';

export class LoginPage {
    constructor(private page: Page) {}

    async goto() {
        await this.page.goto('/');
    }

    async login(username: string, password: string) {
        await this.page.waitForSelector('.login-page');
        await this.page.fill('input[placeholder="用户名"]', username);
        await this.page.fill('input[placeholder="密码"]', password);
        await this.page.click('button[type="submit"]');
    }

    async waitForLoginSuccess() {
        await this.page.waitForFunction(() => !document.querySelector('.login-page'));
    }

    async getErrorMessage() {
        return (await this.page.locator('.ant-message-notice-content').textContent()) || '';
    }

    async isLoginPageVisible() {
        return this.page.locator('.login-page').isVisible();
    }
}
