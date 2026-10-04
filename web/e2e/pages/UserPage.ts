import {Page} from '@playwright/test';

export class UserPage {
    constructor(private page: Page) {}

    async goto() {
        await this.page.goto('/#/system/user');
        await this.page.waitForSelector('.oa-pro-table');
    }

    async clickAdd() {
        await this.page.click('button:has-text("新增")');
        await this.page.waitForSelector('.ant-modal', {state: 'visible'});
    }

    async fillUserForm(data: { name: string; account: string; phone?: string; email?: string; enabled?: boolean }) {
        await this.page.fill('.ant-modal input[id*="name"]', data.name);
        await this.page.fill('.ant-modal input[id*="account"]', data.account);
        if (data.phone) {
            await this.page.fill('.ant-modal input[id*="phone"]', data.phone);
        }
        if (data.email) {
            await this.page.fill('.ant-modal input[id*="email"]', data.email);
        }
        if (data.enabled !== undefined) {
            await this.page.locator('.ant-modal .ant-form-item').filter({hasText: '启用状态'}).locator('.ant-select-content').click();
            await this.page.locator('.ant-select-item-option[title="是"]').click();
            await this.page.waitForTimeout(300);
        }
    }

    async selectOrg() {
        await this.page.click('.ant-modal .ant-tree-select .ant-select-content');
        await this.page.waitForSelector('.ant-tree-select-dropdown', {state: 'visible'});
        await this.page.locator('.ant-tree-select-dropdown .ant-select-tree-treenode:not([aria-hidden="true"])').first().click();
    }

    async submitForm() {
        await this.page.locator('.ant-modal:has(.ant-modal-title:has-text("系统用户")) .ant-btn-primary').click();
        await this.page.locator('.ant-modal-title:has-text("系统用户")').waitFor({state: 'hidden', timeout: 15000});
    }

    async waitForTableLoaded() {
        await this.page.waitForSelector('.ant-table-row');
    }

    async findRowByText(text: string) {
        return this.page.locator(`.ant-table-row:has-text("${text}")`);
    }

    async clickEdit(text: string) {
        const row = this.page.locator(`.ant-table-row:has-text("${text}")`);
        await row.locator('button:has-text("编辑")').click();
        await this.page.locator('.ant-modal-title:has-text("系统用户")').waitFor({state: 'visible', timeout: 15000});
    }

    async clickDelete(text: string) {
        const row = this.page.locator(`.ant-table-row:has-text("${text}")`);
        const deleteButton = row.locator('button:has-text("删除")');
        if ((await deleteButton.count()) > 0) {
            await deleteButton.click();
        } else {
            await row.locator('button:has(.anticon-ellipsis)').click();
            await this.page
                .locator('.ant-dropdown-menu-item:has-text("删除")')
                .click();
        }
        await this.page.click('.ant-popconfirm .ant-btn-primary');
    }
}
