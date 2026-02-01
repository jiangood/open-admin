/**
 * 主题相关的工具类
 */
export class ThemeUtils {
    // 使用 readonly 属性来定义主题常量，保持其不可变性
    public static readonly theme = {
        "primary-color": "#1961AC",
        "success-color": "#52c41a",
        "warning-color": "#faad14",
        "error-color": "#ff4d4f",
        "background-color": "#f5f5f5",

        "primary-color-hover": "#4990CD",
        "primary-color-click": "#124B93"
    };

    /**
     * @description 可选：提供一个获取主题颜色的静态方法，增强工具类的可用性
     * @param key 主题颜色键名
     * @returns 对应的颜色值，如果键名不存在则返回undefined
     */
    public static getColor(key: keyof typeof ThemeUtils.theme): string | undefined {
        return ThemeUtils.theme[key];
    }
}


