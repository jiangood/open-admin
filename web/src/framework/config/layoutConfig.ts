import { EventBus } from '../utils/EventBus';

export type LayoutMode = 'tabs' | 'single';

const LAYOUT_MODE_KEY = 'oa-layout-tabs';

/** 页签模式（多页签）/ 单页模式 */
export const DEFAULT_LAYOUT_MODE: LayoutMode = 'tabs';

const LAYOUT_MODES: LayoutMode[] = ['tabs', 'single'];

function readStoredLayoutMode(): LayoutMode | null {
    if (typeof window === 'undefined') return null;
    try {
        const mode = localStorage.getItem(LAYOUT_MODE_KEY);
        if (mode && LAYOUT_MODES.includes(mode as LayoutMode)) return mode as LayoutMode;
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return null;
}

/** 用户是否在本地自定义过页签模式 */
export function hasStoredLayoutMode(): boolean {
    return readStoredLayoutMode() !== null;
}

function removeStoredLayoutMode() {
    if (typeof window === 'undefined') return;
    try {
        localStorage.removeItem(LAYOUT_MODE_KEY);
    } catch {
        // 忽略
    }
}

/** 全局默认布局（来自系统参数，未自定义用户使用） */
let defaultLayoutMode: LayoutMode = DEFAULT_LAYOUT_MODE;

let currentLayoutMode: LayoutMode = readStoredLayoutMode() ?? DEFAULT_LAYOUT_MODE;

/** 当前页签模式：'tabs' 多页签 / 'single' 单页 */
export function getLayoutMode(): LayoutMode {
    return currentLayoutMode;
}

export function getDefaultLayoutMode(): LayoutMode {
    return defaultLayoutMode;
}

/**
 * 应用系统参数下发的全局默认页签模式。
 * 仅在用户本地没有自定义时生效；已自定义用户保持个人选择。
 */
export function setDefaultLayoutMode(mode: LayoutMode) {
    if (!LAYOUT_MODES.includes(mode)) return;
    defaultLayoutMode = mode;
    if (hasStoredLayoutMode() || currentLayoutMode === mode) return;

    currentLayoutMode = mode;
    EventBus.emit('layoutChange', mode);
}

/** 设置页签模式，持久化并广播 layoutChange 供管理布局重渲染 */
export function setLayoutMode(mode: LayoutMode) {
    if (!LAYOUT_MODES.includes(mode) || currentLayoutMode === mode) return;
    currentLayoutMode = mode;
    if (typeof window !== 'undefined') {
        try {
            localStorage.setItem(LAYOUT_MODE_KEY, mode);
        } catch {
            // 忽略
        }
    }
    EventBus.emit('layoutChange', mode);
}

export function toggleLayoutMode(): LayoutMode {
    const next: LayoutMode = currentLayoutMode === 'tabs' ? 'single' : 'tabs';
    setLayoutMode(next);
    return next;
}

/** 恢复默认页签模式：清除本地覆盖，回到系统参数的全局默认 */
export function resetLayoutMode() {
    removeStoredLayoutMode();
    if (currentLayoutMode === defaultLayoutMode) return;

    currentLayoutMode = defaultLayoutMode;
    EventBus.emit('layoutChange', currentLayoutMode);
}
