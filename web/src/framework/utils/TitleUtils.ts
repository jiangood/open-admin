import {GlobalData} from '../GlobalData';

let currentPageTitle: string | undefined;

function normalize(title?: string): string | undefined {
    return typeof title === 'string' && title.trim() ? title.trim() : undefined;
}

function systemTitle(): string {
    const siteInfo = GlobalData.getSiteInfo();
    // 浏览器标签宽度有限，优先用系统简称，避免挤占页面标题
    return normalize(siteInfo?.shortTitle) || normalize(siteInfo?.title) || 'open-admin';
}

function renderDocumentTitle(): void {
    const next = currentPageTitle ? `${systemTitle()} | ${currentPageTitle}` : systemTitle();
    if (document.title !== next) {
        document.title = next;
    }
}

/**
 * 设置当前页面标题（不含系统标题）；传空只显示系统标题。
 * 浏览器标题格式：`系统标题（优先系统简称） | 页面标题`
 */
export function setPageTitle(title?: string): void {
    currentPageTitle = normalize(title);
    renderDocumentTitle();
}

/** 站点标题变化后重新渲染文档标题 */
export function refreshDocumentTitle(): void {
    renderDocumentTitle();
}

/** 当前页面标题（不含系统标题）；未设置时返回 undefined */
export function getPageTitle(): string | undefined {
    return currentPageTitle;
}
