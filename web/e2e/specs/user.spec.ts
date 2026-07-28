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

    test('should create, edit, and delete a user', async ({page}) => {
        test.setTimeout(120000);
        const testAccount = `e2e_${Date.now()}`;
        const updatedPhone = '13900139000';
        const userPage = new UserPage(page);
        await userPage.goto();

        // Create
        await userPage.clickAdd();
        await userPage.fillUserForm({
            name: 'E2E Test User',
            account: testAccount,
            phone: '13800138000',
            email: `${testAccount}@test.com`,
            enabled: true,
        });
        await userPage.selectOrg();
        await userPage.submitForm();

        const successModal = page.locator('.ant-modal:has(.ant-modal-title:has-text("添加用户成功"))');
        if (await successModal.isVisible({timeout: 5000}).catch(() => false)) {
            await successModal.locator('.ant-btn-primary').click();
            await successModal.waitFor({state: 'hidden', timeout: 5000});
        }

        await userPage.waitForTableLoaded();
        await expect(
            page.locator(`.ant-table-cell:has-text("${testAccount}")`).first()
        ).toBeVisible({timeout: 10000});

        // Edit: update phone number
        await userPage.clickEdit(testAccount);
        await userPage.fillUserForm({name: 'E2E Test User', account: testAccount, phone: updatedPhone, enabled: true});
        await userPage.submitForm();
        await userPage.waitForTableLoaded();
        await expect(
            page.locator(`.ant-table-cell:has-text("${updatedPhone}")`).first()
        ).toBeVisible({timeout: 5000});

        // Delete
        await userPage.clickDelete(testAccount);
        await expect(
            page.locator(`.ant-table-cell:has-text("${testAccount}")`)
        ).toHaveCount(0);
    });
});
