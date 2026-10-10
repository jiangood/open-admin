export {
    getThemeConfig,
    getSiderThemeConfig,
    getToken,
    setThemeColors,
    getThemeMode,
    setThemeMode,
    toggleThemeMode,
    applyThemePreset,
    getActivePresetKey,
    applyDensityPreset,
    getActiveDensityKey,
    applyRadiusPreset,
    getActiveRadiusKey,
    resetUserTheme,
    DEFAULT_COLORS,
    DEFAULT_PRESET_KEY,
    DEFAULT_SIDER_BG,
    DEFAULT_SIDER_TRIGGER_BG,
    THEME_PRESETS,
    DEFAULT_DENSITY_KEY,
    DENSITY_PRESETS,
    DEFAULT_RADIUS_KEY,
    RADIUS_PRESETS,
} from './themeConfig'
export type { ThemeColors, ThemeMode, ThemePreset, ThemeDensity, ThemeRadius, ThemeDensityPreset, ThemeRadiusPreset } from './themeConfig'

export {
    getLayoutMode,
    setLayoutMode,
    toggleLayoutMode,
    resetLayoutMode,
    DEFAULT_LAYOUT_MODE,
} from './layoutConfig'
export type { LayoutMode } from './layoutConfig'

import { resetUserTheme } from './themeConfig'
import { resetLayoutMode } from './layoutConfig'

/** 一键恢复全部界面设置：主题（颜色/明暗/密度/圆角）+ 布局 */
export function resetAllSettings() {
    resetUserTheme()
    resetLayoutMode()
}
