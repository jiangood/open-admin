export class ThemeUtils {
    static defaultTheme = {
        "primary-color": "#1961AC",
        "success-color": "#52c41a",
        "warning-color": "#faad14",
        "error-color": "#ff4d4f",
        "background-color": "#f5f5f5",
        "primary-color-hover": "#4990CD",
        "primary-color-click": "#124B93"
    };

    static cachedTheme = null;

    static get theme() {
        if (this.cachedTheme) return this.cachedTheme;
        this.cachedTheme = this.loadTheme();
        return this.cachedTheme;
    }

    /** 调亮/调暗十六进制颜色 */
    static _lightenHex(hex, percent) {
        const num = parseInt(hex.replace('#', ''), 16);
        const r = num >> 16 & 255, g = num >> 8 & 255, b = num & 255;
        const t = percent < 0 ? 0 : 255;
        const p = Math.abs(percent) / 100;
        return '#' + (0x1000000 + (Math.round((t - r) * p) + r) * 0x10000
            + (Math.round((t - g) * p) + g) * 0x100 + (Math.round((t - b) * p) + b)).toString(16).slice(1);
    }

    static loadTheme() {
        const result = {...this.defaultTheme};
        try {
            const primaryColor = import.meta.env.VITE_THEME_PRIMARY_COLOR;
            const successColor = import.meta.env.VITE_THEME_SUCCESS_COLOR;
            const warningColor = import.meta.env.VITE_THEME_WARNING_COLOR;
            const errorColor = import.meta.env.VITE_THEME_ERROR_COLOR;
            const backgroundColor = import.meta.env.VITE_THEME_BACKGROUND_COLOR;
            if (primaryColor) {
                result["primary-color"] = primaryColor;
                result["primary-color-hover"] = ThemeUtils._lightenHex(primaryColor, 20);
                result["primary-color-click"] = ThemeUtils._lightenHex(primaryColor, -10);
            }
            if (successColor) result["success-color"] = successColor;
            if (warningColor) result["warning-color"] = warningColor;
            if (errorColor) result["error-color"] = errorColor;
            if (backgroundColor) result["background-color"] = backgroundColor;
        } catch (e) {}
        return result;
    }

    static getColor(key) {
        return ThemeUtils.theme[key];
    }
}
