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

        // 工具栏按钮
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("裁切")')).toBeVisible();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("压缩")')).toBeVisible();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("改尺寸")')).toBeVisible();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("查看原图")')).toBeVisible();

        // 画布显示处理结果
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('text=处理结果')).toBeVisible();

        // 直接确定上传
        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should view original then crop via properties panel and upload', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, '1x1.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        // 查看原图：画布切换到原图信息
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("查看原图")').click();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('text=原图信息')).toBeVisible();
        // 再点回到处理结果
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("查看原图")').click();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('text=处理结果')).toBeVisible();

        // 进入裁切：画布出现 Cropper，属性栏出现确认/取消
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("裁切")').click();
        await expect(page.locator('.ant-modal:has-text("图片处理") .cropper-container')).toBeVisible();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("确认裁切")')).toBeVisible();

        // 确认裁切后回到处理结果
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("确认裁切")').click();
        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('text=处理结果')).toBeVisible();

        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();
        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should compress and resize via properties panel', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, '4x3.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        // 压缩：属性栏出现滑块 + 应用
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("压缩")').click();
        await expect(page.locator('.ant-modal:has-text("图片处理") .ant-slider')).toBeVisible();
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("应用")').click();

        // 改尺寸：属性栏出现宽高输入
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("改尺寸")').click();
        await expect(page.locator('.ant-modal:has-text("图片处理") .ant-input-number')).toBeVisible();
        await page.locator('.ant-modal:has-text("图片处理")').locator('button:has-text("应用")').click();

        await expect(page.locator('.ant-modal:has-text("图片处理")').locator('text=处理结果')).toBeVisible();

        await page.locator('.ant-modal:has(.ant-modal-title:has-text("图片处理")) .ant-btn-primary:has-text("确定")').click();
        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'hidden', timeout: 15000});
        await expect(page.locator('.ant-modal:has(.ant-modal-title:has-text("文章")) img')).toBeVisible();
    });

    test('should warn when image exceeds recommended size', async ({page}) => {
        await page.goto('/#/system/article');
        await page.waitForSelector('.oa-pro-table');

        await page.click('button:has-text("新增")');
        await page.locator('.ant-modal-title:has-text("文章")').waitFor({state: 'visible'});

        await page.fill('.ant-modal input[id*="code"]', 'e2e-img-' + Date.now());
        await page.fill('.ant-modal input[id*="title"]', 'E2E图片上传');

        const fileInput = page.locator('.ant-modal input[type="file"]').first();
        await fileInput.setInputFiles(path.join(FIXTURES, 'large.png'));

        await page.locator('.ant-modal-title:has-text("图片处理")').waitFor({state: 'visible', timeout: 15000});

        // 超过 1920x1080 应出现警告
        await expect(page.locator('.ant-modal:has-text("图片超出推荐规格")')).toBeVisible();
    });
});