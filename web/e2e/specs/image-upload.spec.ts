import {test, expect} from '@playwright/test';
import path from 'path';
import {fileURLToPath} from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const FIXTURES = path.join(__dirname, '../fixtures');

test.describe('FieldUploadImage', () => {
    test('should upload 4:3 image directly without crop', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        // 填写必填项
        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        // 选择 4:3 图片，触发弹窗预览
        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, '4x3.png'));

        // 弹窗预览：原图/压缩图/缩略图三栏
        await page.locator('.ant-modal-title:has-text("图片预览")').waitFor({state: 'visible', timeout: 15000});
        await expect(page.locator('.ant-modal:has-text("图片预览")').locator('text=原图')).toBeVisible();
        await expect(page.locator('.ant-modal:has-text("图片预览")').locator('text=压缩图')).toBeVisible();
        await expect(page.locator('.ant-modal:has-text("图片预览")').locator('text=缩略图')).toBeVisible();

        // 4:3 比例匹配，确定按钮应可用，直接上传
        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片预览")) .ant-btn-primary:has-text("确定")').click();

        // 上传成功后弹窗关闭，表单回显缩略图（80x80 图片）
        await page.locator('.ant-modal-title:has-text("图片预览")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should require crop when aspect ratio mismatches', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        // 选择 1:1 图片，比例与 800x600 (4:3) 不符
        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, '1x1.png'));

        await page.locator('.ant-modal-title:has-text("图片预览")').waitFor({state: 'visible', timeout: 15000});

        // 比例不符：出现警告，确定按钮禁用
        await expect(page.locator('.ant-modal:has-text("图片比例与目标尺寸不符")')).toBeVisible();
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("图片预览")) .ant-btn-primary:has-text("确定")')).toBeDisabled();

        // 点击裁切，进入裁切界面
        await page.locator('.ant-modal:has-text("图片预览")').locator('button:has-text("裁切")').click();
        await expect(page.locator('.ant-modal:has-text("图片预览")').locator('button:has-text("确认裁切")')).toBeVisible();

        // 确认裁切后回到预览，确定按钮可用
        await page.locator('.ant-modal:has-text("图片预览")').locator('button:has-text("确认裁切")').click();
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("图片预览")) .ant-btn-primary:has-text("确定")')).toBeEnabled();
    });
});
