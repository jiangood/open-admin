import { EventBus } from '../utils/EventBus';

export type LayoutMode = 'tabs' | 'single';

const LAYOUT_MODE_KEY = 'oa-layout-tabs';

export const DEFAULT_LAYOUT_MODE: LayoutMode = 'tabs';

const LAYOUT_MODES: LayoutMode[] = ['tabs', 'single'];

function readStoredLayoutMode(): LayoutMode {
    if (typeof window === 'undefined') return DEFAULT_LAYOUT_MODE;
    try {
        const mode = localStorage.getItem(LAYOUT_MODE_KEY);
        if (mode && LAYOUT_MODES.includes(mode as LayoutMode)) return mode as LayoutMode;
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return DEFAULT_LAYOUT_MODE;
}

let currentLayoutMode: LayoutMode = readStoredLayoutMode();

/** 当前布局模式：'tabs' 多标签页 / 'single' 单页 */
export function getLayoutMode(): LayoutMode {
    return currentLayoutMode;
}

/** 设置布局模式，持久化并广播 layoutChange 供管理布局重渲染 */
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

/** 恢复默认布局（多标签页） */
export function resetLayoutMode() {
    setLayoutMode(DEFAULT_LAYOUT_MODE);
}
