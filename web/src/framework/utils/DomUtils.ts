/**
 * @fileoverview DOM 操作工具类
 */

/**
 * 封装了获取元素位置和尺寸的工具方法。
 */
export class DomUtils {
    /**
     * 获取元素相对于视口的偏移量（top 和 left）。
     *
     * @param el 目标 DOM 元素或 window 对象。
     * @returns 包含 top 和 left 属性的对象。
     */
    static offset(el: Element | Window): { top: number; left: number } {
        if (el === window) {
            return { top: 0, left: 0 };
        }

        // 类型断言，确保 el 是 Element 类型，以便调用 getBoundingClientRect
        const element = el as Element;
        const { top, left } = element.getBoundingClientRect();

        return { top, left };
    }

    /**
     * 获取元素的外部高度（如果是 window，则返回视口高度）。
     *
     * @param el 目标 DOM 元素或 window 对象。
     * @returns 元素的高度（以像素为单位）。
     */
    static height(el: Element | Window): number {
        return el === window
            ? window.innerHeight // 视口高度
            : (el as Element).getBoundingClientRect().height; // 元素高度
    }

    /**
     * 获取元素的外部宽度（如果是 window，则返回视口宽度）。
     *
     * @param el 目标 DOM 元素或 window 对象。
     * @returns 元素的宽度（以像素为单位）。
     */
    static width(el: Element | Window): number {
        return el === window
            ? window.innerWidth // 视口宽度
            : (el as Element).getBoundingClientRect().width; // 元素宽度
    }
}
