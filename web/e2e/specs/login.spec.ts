import {test, expect} from '@playwright/test';
import {LoginPage} from '../pages/LoginPage';

test.describe('Login', () => {
    test('should login with valid admin credentials', async ({page}) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await page.waitForSelector('.login-page');
        expect(await loginPage.isLoginPageVisible()).toBeTruthy();

        await loginPage.login('admin', 'Open@1234');
        await loginPage.waitForLoginSuccess();

        expect(page.url()).not.toContain('/login');
    });

    test('should show validation error with empty fields', async ({page}) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();

        await page.fill('input[placeholder="用户名"]', '');
        await page.fill('input[placeholder="密码"]', '');
        await page.click('button[type="submit"]');

        const formError = page.locator('.ant-form-item-explain-error');
        await expect(formError.first()).toBeAttached({timeout: 5000});
    });

    test('should stay on login page with wrong password', async ({page}) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login('admin', 'wrongpassword');

        expect(await loginPage.isLoginPageVisible()).toBeTruthy();
    });
});
