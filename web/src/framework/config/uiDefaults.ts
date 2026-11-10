import { setDefaultThemePreset } from './themeConfig';
import { setDefaultLayoutMode } from './layoutConfig';

/** 系统参数中「默认界面」分组的配置键 */
export const UI_THEME_PRESET_KEY = 'sys.ui.theme-preset';
export const UI_TABS_ENABLED_KEY = 'sys.ui.tabs-enabled';

/**
 * 应用后端下发的全局默认界面配置（来自 site-info.config）。
 *
 * 只对未在本地自定义过的用户生效；已自定义用户保持个人设置，
 * 其「恢复默认」会回到这里设置的全局默认值。
 */
export function applyServerUiDefaults(config?: Record<string, unknown> | null) {
    if (!config) return;

    const preset = config[UI_THEME_PRESET_KEY];
    if (typeof preset === 'string') {
        setDefaultThemePreset(preset);
    }

    const tabsEnabled = config[UI_TABS_ENABLED_KEY];
    if (typeof tabsEnabled === 'boolean') {
        setDefaultLayoutMode(tabsEnabled ? 'tabs' : 'single');
    }
}
