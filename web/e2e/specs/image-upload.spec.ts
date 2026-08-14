import {test, expect} from '@playwright/test';
import path from 'path';
import {fileURLToPath} from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const FIXTURES = path.join(__dirname, '../fixtures');

test.describe('FieldUploadImage', () => {
    test('should show canvas + toolbar + properties and upload directly', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        // 选择 4:3 图片，弹窗默认显示画布 + 工具栏
        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, '4x3.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        const modal = page.locator('.ant-modal:has-text("图片处理")');

        // 工具栏按钮
        await expect(modal.locator('button:has-text("裁切")')).toBeVisible();
        await expect(modal.locator('button:has-text("重置")')).toBeVisible();

        // 属性栏：图片信息 + 压缩处理
        await expect(modal.getByText('图片信息', {exact: true})).toBeVisible();
        await expect(modal.getByText('压缩处理', {exact: true})).toBeVisible();
        await expect(modal.getByText('最大宽度', {exact: true})).toBeVisible();
        await expect(modal.getByText('最大体积', {exact: true})).toBeVisible();

        // 小图无需压缩
        await expect(modal.locator('button:has-text("无需压缩")')).toBeVisible();

        // 直接确定上传
        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should crop via properties panel and upload', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, '1x1.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        const modal = page.locator('.ant-modal:has-text("图片处理")');

        // 默认显示图片信息
        await expect(modal.getByText('图片信息', {exact: true})).toBeVisible();

        // 进入裁切：画布出现 Cropper，属性栏出现裁切信息 + 比例 + 确认/取消
        await modal.locator('button:has-text("裁切")').click();
        await expect(modal.locator('.cropper-container')).toBeVisible();
        await expect(modal.getByText('裁切信息', {exact: true})).toBeVisible();
        await expect(modal.locator('.ant-radio-wrapper:has-text("4:3")')).toBeVisible();
        await expect(modal.locator('button:has-text("确认裁切")')).toBeVisible();

        // 确认裁切后回到图片信息
        await modal.locator('button:has-text("确认裁切")').click();
        await expect(modal.getByText('图片信息', {exact: true})).toBeVisible();

        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();
        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should compress oversized image via properties panel and upload', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, 'large.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        const modal = page.locator('.ant-modal:has-text("图片处理")');

        // 大图出现推荐压缩
        await expect(modal.locator('button:has-text("推荐压缩")')).toBeVisible();

        // 点击推荐压缩后，压缩完成回到无需压缩状态
        await modal.locator('button:has-text("推荐压缩")').click();
        await expect(modal.locator('button:has-text("无需压缩")')).toBeVisible();

        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();
        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should recommend compression for oversized image and allow direct upload', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, 'large.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        const modal = page.locator('.ant-modal:has-text("图片处理")');

        // 超过 1920px 目标宽度应提示推荐压缩
        await expect(modal.locator('button:has-text("推荐压缩")')).toBeVisible();

        // 未压缩也可直接确定上传
        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();
        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });
});
