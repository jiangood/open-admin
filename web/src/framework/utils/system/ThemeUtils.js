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
        let custom = {};
        try {
            if (typeof OPEN_ADMIN_THEME !== 'undefined' && OPEN_ADMIN_THEME) {
                custom = OPEN_ADMIN_THEME;
            }
        } catch (e) {}
        const result = {...this.defaultTheme, ...custom};
        if (custom["primary-color"]) {
            if (!custom["primary-color-hover"]) {
                result["primary-color-hover"] = ThemeUtils._lightenHex(result["primary-color"], 20);
            }
            if (!custom["primary-color-click"]) {
                result["primary-color-click"] = ThemeUtils._lightenHex(result["primary-color"], -10);
            }
        }
        return result;
    }

    static getColor(key) {
        return ThemeUtils.theme[key];
    }
}
