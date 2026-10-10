import { theme } from 'antd';
import type { ThemeConfig } from 'antd';
import { EventBus } from '../utils/EventBus';

const { getDesignToken } = theme;

export type ThemeMode = 'light' | 'dark';

export interface ThemeColors {
    colorPrimary?: string;
    colorSuccess?: string;
    colorWarning?: string;
    colorError?: string;
    colorInfo?: string;
    colorBgLayout?: string;
    /** 侧栏底色（深色系）；不设置时使用 antd 默认值 */
    siderBg?: string;
    /** 侧栏子菜单底色；缺省与 siderBg 相同 */
    siderSubBg?: string;
    /** 侧栏菜单项悬停底色；缺省 antd 默认（transparent） */
    siderHoverBg?: string;
    /** 侧栏折叠触发条底色；不设置时使用 antd 默认值 */
    siderTriggerBg?: string;
}

export interface ThemePreset {
    key: string;
    name: string;
    colors: ThemeColors;
}

export type ThemeDensity = 'default' | 'compact';
export type ThemeRadius = 'sharp' | 'default' | 'round';

export interface ThemeDensityPreset {
    key: ThemeDensity;
    name: string;
    /** 是否叠加 antd 紧凑算法 */
    compact?: boolean;
    /** 追加的 Seed/Map Token 覆盖 */
    token?: Record<string, unknown>;
}

export interface ThemeRadiusPreset {
    key: ThemeRadius;
    name: string;
    token: Record<string, unknown>;
}

/** 框架默认颜色，回归 antd 原生主题（不再覆盖主色） */
export const DEFAULT_COLORS: ThemeColors = {
    colorPrimary: '#1677ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#1677ff',
    colorBgLayout: '#f5f5f5',
};

export const DEFAULT_PRESET_KEY = 'antd';

/** 界面密度预设：默认保持 antd 原生，紧凑叠加 compactAlgorithm */
export const DENSITY_PRESETS: ThemeDensityPreset[] = [
    { key: 'default', name: '默认' },
    {
        key: 'compact',
        name: '紧凑',
        compact: true,
        token: { sizeUnit: 4, sizeStep: 4, controlHeight: 28, fontSize: 13 },
    },
];

export const DEFAULT_DENSITY_KEY: ThemeDensity = 'default';

/** 圆角预设：默认回归 antd 原生 6 */
export const RADIUS_PRESETS: ThemeRadiusPreset[] = [
    { key: 'default', name: '默认', token: { borderRadius: 6 } },
    { key: 'sharp', name: '直角', token: { borderRadius: 0 } },
    { key: 'round', name: '圆润', token: { borderRadius: 10 } },
];

export const DEFAULT_RADIUS_KEY: ThemeRadius = 'default';

/** 可选主题预设；antd 预设不做任何颜色覆盖，保持 100% 原生观感 */
export const THEME_PRESETS: ThemePreset[] = [
    {
        key: 'antd',
        name: 'Ant Design 默认',
        colors: {},
    },
    {
        key: 'cyan',
        name: '青碧',
        colors: {
            colorPrimary: '#13c2c2',
            siderBg: '#0C2E2E',
            siderSubBg: '#0C2E2E',
            siderTriggerBg: '#082222',
            siderHoverBg: 'rgba(255,255,255,0.06)',
        },
    },
    {
        key: 'green',
        name: '翠竹',
        colors: {
            colorPrimary: '#389e0d',
            siderBg: '#10291A',
            siderSubBg: '#10291A',
            siderTriggerBg: '#0B1E13',
            siderHoverBg: 'rgba(255,255,255,0.06)',
        },
    },
    {
        key: 'purple',
        name: '紫罗兰',
        colors: {
            colorPrimary: '#722ed1',
            siderBg: '#1E1440',
            siderSubBg: '#1E1440',
            siderTriggerBg: '#170F31',
            siderHoverBg: 'rgba(255,255,255,0.06)',
        },
    },
    {
        key: 'red',
        name: '中国红',
        colors: {
            colorPrimary: '#cf1322',
            siderBg: '#2A1418',
            siderSubBg: '#2A1418',
            siderTriggerBg: '#201014',
            siderHoverBg: 'rgba(255,255,255,0.06)',
        },
    },
    {
        key: 'orange',
        name: '暖橙',
        colors: {
            colorPrimary: '#d46b08',
            siderBg: '#2A1D10',
            siderSubBg: '#2A1D10',
            siderTriggerBg: '#20160C',
            siderHoverBg: 'rgba(255,255,255,0.06)',
        },
    },
    {
        key: 'graphite',
        name: '石墨灰',
        colors: {
            colorPrimary: '#2f3542',
            siderBg: '#1B2026',
            siderSubBg: '#1B2026',
            siderTriggerBg: '#141A20',
            siderHoverBg: 'rgba(255,255,255,0.06)',
        },
    },
];

const THEME_MODE_KEY = 'oa-theme-mode';
const THEME_PRESET_KEY = 'oa-theme-preset';
const THEME_DENSITY_KEY = 'oa-theme-density';
const THEME_RADIUS_KEY = 'oa-theme-radius';

/** 业务侧传入的颜色基线（由 setThemeColors 设置） */
let businessColors: Partial<ThemeColors> | null = null;
/** 用户选择的预设 key（持久化到 localStorage） */
let presetKey: string = readStoredPreset();
let densityKey: ThemeDensity = readStoredDensity();
let radiusKey: ThemeRadius = readStoredRadius();
let currentMode: ThemeMode = readStoredMode();
let cachedConfig: ThemeConfig | null = null;
let cachedSiderConfig: ThemeConfig | null = null;
let cachedToken: Record<string, unknown> | null = null;

function readStoredMode(): ThemeMode {
    if (typeof window === 'undefined') return 'light';
    try {
        return localStorage.getItem(THEME_MODE_KEY) === 'dark' ? 'dark' : 'light';
    } catch {
        return 'light';
    }
}

function readStoredPreset(): string {
    if (typeof window === 'undefined') return DEFAULT_PRESET_KEY;
    try {
        const key = localStorage.getItem(THEME_PRESET_KEY);
        if (key && THEME_PRESETS.some(p => p.key === key)) return key;
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return DEFAULT_PRESET_KEY;
}

function persistPreset(key: string) {
    if (typeof window === 'undefined') return;
    try {
        localStorage.setItem(THEME_PRESET_KEY, key);
    } catch {
        // 忽略
    }
}

function readStoredDensity(): ThemeDensity {
    if (typeof window === 'undefined') return DEFAULT_DENSITY_KEY;
    try {
        const key = localStorage.getItem(THEME_DENSITY_KEY);
        if (key && DENSITY_PRESETS.some(p => p.key === key)) return key as ThemeDensity;
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return DEFAULT_DENSITY_KEY;
}

function readStoredRadius(): ThemeRadius {
    if (typeof window === 'undefined') return DEFAULT_RADIUS_KEY;
    try {
        const key = localStorage.getItem(THEME_RADIUS_KEY);
        if (key && RADIUS_PRESETS.some(p => p.key === key)) return key as ThemeRadius;
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return DEFAULT_RADIUS_KEY;
}

function writeStored(key: string, value: string) {
    if (typeof window === 'undefined') return;
    try {
        localStorage.setItem(key, value);
    } catch {
        // 忽略
    }
}

function getActiveDensity(): ThemeDensityPreset | undefined {
    return DENSITY_PRESETS.find(p => p.key === densityKey);
}

function getActiveRadius(): ThemeRadiusPreset | undefined {
    return RADIUS_PRESETS.find(p => p.key === radiusKey);
}

/** 尺寸维度 Token 合并：密度 ← 圆角（用户显式选择优先） */
function getMergedSizeToken(): Record<string, unknown> {
    return {
        ...(getActiveDensity()?.token || {}),
        ...(getActiveRadius()?.token || {}),
    };
}

function getActivePreset(): ThemePreset | undefined {
    return THEME_PRESETS.find(p => p.key === presetKey);
}

/** 合并优先级：antd 默认 ← 业务基线 ← 用户预设 */
function getMergedColors(): ThemeColors {
    return {
        ...DEFAULT_COLORS,
        ...(businessColors || {}),
        ...(getActivePreset()?.colors || {}),
    };
}

function hasExplicitBgLayout(): boolean {
    return businessColors?.colorBgLayout != null || getActivePreset()?.colors.colorBgLayout != null;
}

function invalidate() {
    cachedConfig = null;
    cachedToken = null;
    cachedSiderConfig = null;
}

function buildAlgorithm(): ThemeConfig['algorithm'] {
    const algorithms = [currentMode === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm];
    if (getActiveDensity()?.compact) {
        algorithms.push(theme.compactAlgorithm);
    }
    return algorithms;
}

function buildConfig(): ThemeConfig {
    const merged = getMergedColors();
    const algorithm = buildAlgorithm();
    const token: Record<string, unknown> = {
        ...getMergedSizeToken(),
        colorPrimary: merged.colorPrimary,
        colorSuccess: merged.colorSuccess,
        colorWarning: merged.colorWarning,
        colorError: merged.colorError,
        colorInfo: merged.colorInfo,
    };
    // colorBgLayout 仅浅色模式默认覆盖；暗色模式交给算法推导，避免被强制成浅灰。
    // 业务/预设显式传入 colorBgLayout 时两种模式都尊重。
    if (currentMode === 'light' || hasExplicitBgLayout()) {
        token.colorBgLayout = merged.colorBgLayout;
    }

    const resolved = getDesignToken({ token, algorithm } as ThemeConfig);
    const components: Record<string, unknown> = {
        // 顶栏用容器色，跟随明/暗主题
        Layout: { headerBg: resolved.colorBgContainer, triggerHeight: 32 },
    };

    return { token, algorithm, components } as ThemeConfig;
}

/**
 * 侧栏专用主题：单独套一层暗色算法，使分割线、边框、填充等按暗色派生，
 * 避免浅色算法下色值不可见；同时按预设覆盖侧栏底色。
 */
export function getSiderThemeConfig(): ThemeConfig {
    if (cachedSiderConfig) return cachedSiderConfig;

    const merged = getMergedColors();
    const token: Record<string, unknown> = {
        ...getMergedSizeToken(),
        colorPrimary: merged.colorPrimary,
    };
    const layout: Record<string, unknown> = {};
    const menu: Record<string, unknown> = {
        darkItemSelectedBg: merged.colorPrimary,
    };

    if (merged.siderBg) {
        layout.siderBg = merged.siderBg;
        menu.darkItemBg = merged.siderBg;
        menu.darkSubMenuItemBg = merged.siderSubBg || merged.siderBg;
        menu.darkItemHoverBg = merged.siderHoverBg || 'rgba(255,255,255,0.06)';
    }
    if (merged.siderTriggerBg) {
        layout.triggerBg = merged.siderTriggerBg;
    }

    const siderAlgorithms = [theme.darkAlgorithm];
    if (getActiveDensity()?.compact) {
        siderAlgorithms.push(theme.compactAlgorithm);
    }

    cachedSiderConfig = {
        algorithm: siderAlgorithms,
        inherit: true,
        token,
        components: { Layout: layout, Menu: menu },
    } as ThemeConfig;
    return cachedSiderConfig;
}

function setVar(el: HTMLElement, name: string, value: unknown) {
    if (value == null) return;
    el.style.setProperty(name, String(value));
}

/** 像素类 Token 以数值返回（如 32），需补 px 后写入，避免 line-height 被当作倍数 */
function setPxVar(el: HTMLElement, name: string, value: unknown) {
    if (value == null) return;
    const str = typeof value === 'number' ? `${value}px` : String(value);
    el.style.setProperty(name, str);
}

function syncCssVars(token: Record<string, unknown>) {
    if (typeof document === 'undefined') return;
    const merged = getMergedColors();
    const el = document.documentElement;
    setVar(el, '--primary-color', token.colorPrimary);
    setVar(el, '--primary-color-hover', token.colorPrimaryHover || token.colorPrimary);
    // 供 less 中替代硬编码颜色，使自定义样式跟随明/暗主题
    setVar(el, '--oa-color-bg-container', token.colorBgContainer);
    setVar(el, '--oa-color-bg-layout', token.colorBgLayout);
    setVar(el, '--oa-color-border', token.colorBorderSecondary ?? token.colorBorder);
    setVar(el, '--oa-color-text', token.colorText);
    setVar(el, '--oa-color-text-secondary', token.colorTextSecondary);
    setVar(el, '--oa-color-text-tertiary', token.colorTextTertiary);
    setVar(el, '--oa-color-fill', token.colorFillTertiary);
    setVar(el, '--oa-color-primary-bg', token.colorPrimaryBg);
    // 尺寸/圆角变量，供 less 复用，使自定义样式跟随密度与圆角预设
    setPxVar(el, '--oa-border-radius', token.borderRadius);
    setPxVar(el, '--oa-control-height', token.controlHeight);
    setPxVar(el, '--oa-font-size', token.fontSize);
    // 侧栏相关变量
    setVar(el, '--oa-sider-bg', merged.siderBg || '#001529');
    setVar(el, '--oa-sider-text', 'rgba(255,255,255,0.85)');
    setVar(el, '--oa-sider-hover-bg', merged.siderHoverBg || 'transparent');
    setVar(el, '--oa-sider-trigger-bg', merged.siderTriggerBg || '#002140');
    el.dataset.oaTheme = currentMode;
    el.style.colorScheme = currentMode;
}

/** 由 Layouts 在渲染前调用，传入业务侧颜色基线；不传则恢复 antd 默认主题 */
export function setThemeColors(colors?: Partial<ThemeColors>) {
    businessColors = colors || null;
    invalidate();
    syncCssVars(getToken());
}

export function getThemeConfig(): ThemeConfig {
    if (!cachedConfig) {
        cachedConfig = buildConfig();
    }
    return cachedConfig;
}

export function getToken(): ThemeColors & Record<string, unknown> {
    if (!cachedToken) {
        cachedToken = getDesignToken(getThemeConfig()) as unknown as Record<string, unknown>;
    }
    return cachedToken as ThemeColors & Record<string, unknown>;
}

export function getThemeMode(): ThemeMode {
    return currentMode;
}

/** 应用预设主题色，持久化并广播 themeChange 供布局重渲染 */
export function applyThemePreset(key: string) {
    if (!THEME_PRESETS.some(p => p.key === key)) return;
    presetKey = key;
    persistPreset(key);
    invalidate();
    syncCssVars(getToken());
    EventBus.emit('themeChange', currentMode);
}

export function getActivePresetKey(): string {
    return presetKey;
}

/** 应用界面密度预设，持久化并广播 themeChange */
export function applyDensityPreset(key: ThemeDensity) {
    if (!DENSITY_PRESETS.some(p => p.key === key)) return;
    densityKey = key;
    writeStored(THEME_DENSITY_KEY, key);
    invalidate();
    syncCssVars(getToken());
    EventBus.emit('themeChange', currentMode);
}

export function getActiveDensityKey(): ThemeDensity {
    return densityKey;
}

/** 应用圆角预设，持久化并广播 themeChange */
export function applyRadiusPreset(key: ThemeRadius) {
    if (!RADIUS_PRESETS.some(p => p.key === key)) return;
    radiusKey = key;
    writeStored(THEME_RADIUS_KEY, key);
    invalidate();
    syncCssVars(getToken());
    EventBus.emit('themeChange', currentMode);
}

export function getActiveRadiusKey(): ThemeRadius {
    return radiusKey;
}

/** 恢复默认：颜色 / 密度 / 圆角 / 明暗 全部复位（不影响布局，布局由 resetAllSettings 统一处理） */
export function resetUserTheme() {
    presetKey = DEFAULT_PRESET_KEY;
    densityKey = DEFAULT_DENSITY_KEY;
    radiusKey = DEFAULT_RADIUS_KEY;
    currentMode = 'light';
    persistPreset(presetKey);
    writeStored(THEME_DENSITY_KEY, densityKey);
    writeStored(THEME_RADIUS_KEY, radiusKey);
    writeStored(THEME_MODE_KEY, currentMode);
    invalidate();
    syncCssVars(getToken());
    EventBus.emit('themeChange', currentMode);
}

/** 切换明/暗模式：清缓存、同步 CSS 变量并广播 themeChange 供布局重渲染 */
export function setThemeMode(mode: ThemeMode) {
    if (currentMode === mode) return;
    currentMode = mode;
    if (typeof window !== 'undefined') {
        try {
            localStorage.setItem(THEME_MODE_KEY, mode);
        } catch {
            // 隐私模式下 localStorage 不可用，忽略
        }
    }
    invalidate();
    syncCssVars(getToken());
    EventBus.emit('themeChange', mode);
}

export function toggleThemeMode(): ThemeMode {
    const next: ThemeMode = currentMode === 'dark' ? 'light' : 'dark';
    setThemeMode(next);
    return next;
}
