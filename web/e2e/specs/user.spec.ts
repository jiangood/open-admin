import {test, expect} from '@playwright/test';
import {UserPage} from '../pages/UserPage';

test.describe('User Management', () => {
    test('should display user list page', async ({page}) => {
        const userPage = new UserPage(page);
        await userPage.goto();
        await userPage.waitForTableLoaded();

        const rows = page.locator('.ant-table-row');
        const count = await rows.count();
        expect(count).toBeGreaterThan(0);
    });
});
