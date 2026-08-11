import { theme } from 'antd';
import type { ThemeConfig } from 'antd';

const { getDesignToken } = theme;

export interface ThemeColors {
    colorPrimary?: string;
    colorSuccess?: string;
    colorWarning?: string;
    colorError?: string;
    colorBgLayout?: string;
}

export const DEFAULT_COLORS: Required<ThemeColors> = {
    colorPrimary: '#1961AC',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorBgLayout: '#f5f5f5',
};

let overrideColors: Partial<ThemeColors> | null = null;
let cachedConfig: ThemeConfig | null = null;
let cachedToken: Record<string, any> | null = null;

function buildConfig(): ThemeConfig {
    const token: Record<string, any> = { borderRadius: 4, ...DEFAULT_COLORS, ...overrideColors };
    const components: Record<string, any> = {
        Layout: { headerBg: 'white', triggerHeight: 32 },
    };

    return { token, components };
}

function syncCssVars(token: Record<string, any>) {
    if (typeof document === 'undefined') return;
    const el = document.documentElement;
    el.style.setProperty('--primary-color', token.colorPrimary);
    el.style.setProperty('--primary-color-hover', token.colorPrimaryHover || token.colorPrimary);
}

/** 由 Layouts 在渲染前调用，传入业务侧颜色覆盖；不传则恢复默认主题 */
export function setThemeColors(colors?: Partial<ThemeColors>) {
    overrideColors = colors || null;
    cachedConfig = null;
    cachedToken = null;
    syncCssVars(getToken());
}

export function getThemeConfig(): ThemeConfig {
    if (!cachedConfig) {
        cachedConfig = buildConfig();
    }
    return cachedConfig;
}

export function getToken(): Record<string, any> {
    if (!cachedToken) {
        cachedToken = getDesignToken(getThemeConfig());
    }
    return cachedToken;
}
