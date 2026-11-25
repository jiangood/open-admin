import { EventBus } from '../utils/EventBus';

export type MenuMode = 'side' | 'top';

const MENU_MODE_KEY = 'oa-menu-mode';

/**
 * 菜单模式：side 左侧完整菜单树 / top 顶栏一级菜单 + 左侧当前目录子菜单。
 * 移动端（≤768px）固定使用汉堡抽屉导航，不受此设置影响。
 */
export const DEFAULT_MENU_MODE: MenuMode = 'top';

const MENU_MODES: MenuMode[] = ['side', 'top'];

function readStoredMenuMode(): MenuMode | null {
    if (typeof window === 'undefined') return null;
    try {
        const mode = localStorage.getItem(MENU_MODE_KEY);
        if (mode && MENU_MODES.includes(mode as MenuMode)) return mode as MenuMode;
    } catch {
        // 隐私模式下 localStorage 不可用，忽略
    }
    return null;
}

/** 用户是否在本地自定义过菜单模式 */
export function hasStoredMenuMode(): boolean {
    return readStoredMenuMode() !== null;
}

function removeStoredMenuMode() {
    if (typeof window === 'undefined') return;
    try {
        localStorage.removeItem(MENU_MODE_KEY);
    } catch {
        // 忽略
    }
}

/** 全局默认菜单模式（来自系统参数，未自定义用户使用） */
let defaultMenuMode: MenuMode = DEFAULT_MENU_MODE;

let currentMenuMode: MenuMode = readStoredMenuMode() ?? DEFAULT_MENU_MODE;

/** 当前菜单模式。移动端呈现由布局层决定，这里只返回配置值 */
export function getMenuMode(): MenuMode {
    return currentMenuMode;
}

export function getDefaultMenuMode(): MenuMode {
    return defaultMenuMode;
}

/**
 * 应用系统参数下发的全局默认菜单模式。
 * 仅在用户本地没有自定义时生效；已自定义用户保持个人选择。
 */
export function setDefaultMenuMode(mode: MenuMode) {
    if (!MENU_MODES.includes(mode)) return;
    defaultMenuMode = mode;
    if (hasStoredMenuMode() || currentMenuMode === mode) return;

    currentMenuMode = mode;
    EventBus.emit('menuModeChange', mode);
}

/** 设置菜单模式，持久化并广播 menuModeChange 供管理布局重渲染 */
export function setMenuMode(mode: MenuMode) {
    if (!MENU_MODES.includes(mode) || currentMenuMode === mode) return;
    currentMenuMode = mode;
    if (typeof window !== 'undefined') {
        try {
            localStorage.setItem(MENU_MODE_KEY, mode);
        } catch {
            // 忽略
        }
    }
    EventBus.emit('menuModeChange', mode);
}

/** 恢复默认菜单模式：清除本地覆盖，回到系统参数的全局默认 */
export function resetMenuMode() {
    removeStoredMenuMode();
    if (currentMenuMode === defaultMenuMode) return;

    currentMenuMode = defaultMenuMode;
    EventBus.emit('menuModeChange', currentMenuMode);
}
