/**
 * 浏览器指纹。
 *
 * 采集一批相对稳定的设备/浏览器特征（UA、语言、屏幕、时区、硬件信息、Canvas 渲染差异），
 * 哈希成 16 位十六进制字符串并缓存在 localStorage，浏览器保持不变时指纹不变。
 *
 * 用途：前端通过请求头 {@link BROWSER_FINGERPRINT_HEADER} 上报，后端写入操作日志，
 * 便于判断多条操作记录是否来自同一浏览器。指纹可被伪造，仅作辅助排查，不能作为安全凭证。
 */

/** 上报浏览器指纹的请求头名称，需与后端 BrowserFingerprintTool.HEADER 保持一致 */
export const BROWSER_FINGERPRINT_HEADER = 'X-Browser-Fingerprint';

const STORAGE_KEY = 'oa-browser-fingerprint';

/** 会话内缓存，避免每次请求重复计算（localStorage 不可用时也能复用） */
let cachedFingerprint: string | null = null;

/** Canvas 渲染结果，不同设备的字体/抗锯齿/显卡驱动存在差异，可用于区分同 UA 的不同设备 */
function canvasSignature(): string {
    try {
        const canvas = document.createElement('canvas');
        canvas.width = 220;
        canvas.height = 40;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
            return '';
        }
        ctx.textBaseline = 'top';
        ctx.font = '14px Arial';
        ctx.fillStyle = '#f60';
        ctx.fillRect(100, 1, 62, 20);
        ctx.fillStyle = '#069';
        ctx.fillText('open-admin-fingerprint', 2, 15);
        ctx.fillStyle = 'rgba(102, 204, 0, 0.7)';
        ctx.fillText('open-admin-fingerprint', 4, 17);
        return canvas.toDataURL();
    } catch {
        return '';
    }
}

/** 收集参与计算的特征值 */
function collectFeatures(): string {
    const nav = navigator;
    const scr = typeof screen === 'undefined' ? null : screen;
    const timeZone = typeof Intl === 'undefined'
        ? ''
        : Intl.DateTimeFormat().resolvedOptions().timeZone;

    const parts = [
        nav.userAgent,
        nav.language,
        (nav.languages || []).join(','),
        nav.platform,
        String(nav.hardwareConcurrency ?? ''),
        String(nav.maxTouchPoints ?? ''),
        scr ? `${scr.width}x${scr.height}x${scr.colorDepth}x${scr.pixelDepth}` : '',
        String(window.devicePixelRatio ?? ''),
        timeZone,
        canvasSignature(),
    ];
    return parts.join('|');
}

/** cyrb53 变体，输出 16 位十六进制字符串（非加密哈希，足够区分散列即可） */
function hash(input: string): string {
    let h1 = 0xdeadbeef;
    let h2 = 0x41c6ce57;
    for (let i = 0; i < input.length; i++) {
        const ch = input.charCodeAt(i);
        h1 = Math.imul(h1 ^ ch, 2654435761);
        h2 = Math.imul(h2 ^ ch, 1597334677);
    }
    h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507) ^ Math.imul(h2 ^ (h2 >>> 13), 3266489909);
    h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507) ^ Math.imul(h1 ^ (h1 >>> 13), 3266489909);
    return (h2 >>> 0).toString(16).padStart(8, '0') + (h1 >>> 0).toString(16).padStart(8, '0');
}

/**
 * 获取当前浏览器的指纹。
 *
 * 优先使用 localStorage 中已缓存的指纹；首次访问时计算并缓存。
 * 无 DOM（如 SSR/构建期）时返回空串。
 */
export function getBrowserFingerprint(): string {
    if (cachedFingerprint) {
        return cachedFingerprint;
    }
    if (typeof window === 'undefined' || typeof navigator === 'undefined') {
        return '';
    }

    try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
            cachedFingerprint = stored;
            return stored;
        }
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }

    let fingerprint = '';
    try {
        fingerprint = hash(collectFeatures());
    } catch (e) {
        console.warn('[BrowserFingerprint] 计算浏览器指纹失败', e);
    }
    if (!fingerprint) {
        // 极端环境（Canvas 不可用等）退化为随机值，至少保证本次会话可区分
        fingerprint = Math.random().toString(16).slice(2).padEnd(16, '0').slice(0, 16);
    }

    cachedFingerprint = fingerprint;
    try {
        localStorage.setItem(STORAGE_KEY, fingerprint);
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return fingerprint;
}
