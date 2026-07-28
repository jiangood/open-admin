import {test as setup, expect} from '@playwright/test';

const authFile = 'e2e/.auth/user.json';

function encodePassword(pwd: string): string {
    const chars: number[] = [];
    for (let i = 0; i < pwd.length; i++)
        chars.push(pwd.charCodeAt(i) + i + 2);
    return btoa(String.fromCharCode(...chars));
}

setup('authenticate as admin', async ({page}) => {
    await page.goto('/');
    await page.waitForSelector('.login-page');

    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'Open@1234');
    await page.click('button[type="submit"]');

    await page.waitForFunction(() => !document.querySelector('.login-page'));

    await page.context().storageState({path: authFile});
});
