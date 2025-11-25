/**
 * @fileoverview 主题相关的工具类
 * @description 将原对象导出转换为一个Utils类，以Utils结尾
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
     * @description 构造函数私有化，防止类被实例化，强调其作为工具类的作用
     */
    private constructor() {
        // 工具类通常不需要实例化
    }

    /**
     * @description 可选：提供一个获取主题颜色的静态方法，增强工具类的可用性
     * @param key 主题颜色键名
     * @returns 对应的颜色值，如果键名不存在则返回undefined
     */
    public static getColor(key: keyof typeof ThemeUtils.theme): string | undefined {
    return ThemeUtils.theme[key];
}
}

// 示例：如果你需要在其他地方导入并使用主题颜色
// import { ThemeUtils } from './your-file-path';
// const primaryColor = ThemeUtils.theme["primary-color"];
// 或者使用静态方法
// const successColor = ThemeUtils.getColor("success-color");
