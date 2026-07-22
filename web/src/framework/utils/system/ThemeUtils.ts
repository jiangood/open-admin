import {ColorsUtils} from '../ColorsUtils';

declare const OPEN_ADMIN_THEME: Record<string, string> | undefined;

export class ThemeUtils {
    private static readonly defaultTheme: Record<string, string> = {
        "primary-color": "#1961AC",
        "success-color": "#52c41a",
        "warning-color": "#faad14",
        "error-color": "#ff4d4f",
        "background-color": "#f5f5f5",
        "primary-color-hover": "#4990CD",
        "primary-color-click": "#124B93"
    };

    private static cachedTheme: Record<string, string> | null = null;

    static get theme(): Record<string, string> {
        if (this.cachedTheme) return this.cachedTheme;
        this.cachedTheme = this.loadTheme();
        return this.cachedTheme;
    }

    private static loadTheme(): Record<string, string> {
        let custom: Record<string, string> = {};
        try {
            if (typeof OPEN_ADMIN_THEME !== 'undefined' && OPEN_ADMIN_THEME) {
                custom = OPEN_ADMIN_THEME;
            }
        } catch {}

        const result = {...this.defaultTheme, ...custom};

        // Auto-derive hover/click from primary if not explicitly set
        if (custom["primary-color"]) {
            if (!custom["primary-color-hover"]) {
                result["primary-color-hover"] = ColorsUtils.lighten(result["primary-color"], 20);
            }
            if (!custom["primary-color-click"]) {
                result["primary-color-click"] = ColorsUtils.lighten(result["primary-color"], -10);
            }
        }

        return result;
    }

    public static getColor(key: string): string | undefined {
        return ThemeUtils.theme[key];
    }
}


