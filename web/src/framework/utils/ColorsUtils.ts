/**
 * forked form https://github.com/quasarframework/quasar
 */

// 正则表达式用于匹配 rgba 格式的字符串
const reRGBA = /^rgb(a)?\((\d{1,3}),(\d{1,3}),(\d{1,3}),?([01]?\.?\d*?)?\)$/

/**
 * RGB 颜色接口
 * r, g, b 的范围是 [0, 255]
 * a 的范围是 [0, 100] (百分比)
 */
export interface RGB {
    r: number
    g: number
    b: number
    a?: number // 可选的 alpha (百分比)
}

/**
 * HSV 颜色接口
 * h 的范围是 [0, 360]
 * s, v 的范围是 [0, 100] (百分比)
 * a 的范围是 [0, 100] (百分比)
 */
export interface HSV {
    h: number
    s: number
    v: number
    a?: number // 可选的 alpha (百分比)
}

/**
 * 颜色工具类，提供颜色相关的转换和操作方法。
 */
export class ColorsUtils {
    /**
     * 将 RGB 颜色对象转换为十六进制字符串 (包含可选的 alpha 值).
     * @param color RGB 颜色对象
     * @returns 十六进制颜色字符串, 如 '#RRGGBB' 或 '#RRGGBBAA'
     */
    static rgbToHex (color: RGB): string {
        let { r, g, b, a } = color
        const alpha = a !== void 0

        r = Math.round(r)
        g = Math.round(g)
        b = Math.round(b)

        if (
            r > 255
            || g > 255
            || b > 255
            || (alpha && a! > 100)
        ) {
            throw new TypeError('Expected 3 numbers below 256 (and optionally one below 100)')
        }

        const alphaHex = alpha
            ? (Math.round(255 * a! / 100) | 1 << 8).toString(16).slice(1)
            : ''

        return '#' + ((b | g << 8 | r << 16) | 1 << 24).toString(16).slice(1) + alphaHex
    }

    /**
     * 将 RGB 颜色对象转换为 rgb() 或 rgba() 字符串.
     * @param color RGB 颜色对象
     * @returns rgb() 或 rgba() 颜色字符串
     */
    static rgbToString (color: RGB): string {
        const { r, g, b, a } = color
        return `rgb${a !== void 0 ? 'a' : ''}(${r},${g},${b}${a !== void 0 ? ',' + (a / 100) : ''})`
    }

    /**
     * 将十六进制颜色字符串转换为 RGB 颜色对象.
     * 支持 '#RGB', '#RGBA', '#RRGGBB', '#RRGGBBAA' 格式.
     * @param hex 十六进制颜色字符串
     * @returns RGB 颜色对象
     */
    static hexToRgb (hex: string): RGB {
        if (typeof hex !== 'string') {
            throw new TypeError('Expected a string')
        }

        hex = hex.replace(/^#/, '')

        if (hex.length === 3) {
            hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2]
        } else if (hex.length === 4) {
            // 确保 alpha 也被扩展
            hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2] + hex[3] + hex[3]
        }

        const num = parseInt(hex, 16)

        return hex.length > 6
            ? { r: num >> 24 & 255, g: num >> 16 & 255, b: num >> 8 & 255, a: Math.round((num & 255) / 2.55) }
            : { r: num >> 16, g: num >> 8 & 255, b: num & 255 }
    }

    /**
     * 将 HSV 颜色对象转换为 RGB 颜色对象.
     * @param color HSV 颜色对象
     * @returns RGB 颜色对象
     */
    static hsvToRgb (color: HSV): RGB {
        let r: number = 0, g: number = 0, b: number = 0
        let { h, s, v, a } = color
        s = s / 100
        v = v / 100

        h = h / 360
        const
            i = Math.floor(h * 6),
            f = h * 6 - i,
            p = v * (1 - s),
            q = v * (1 - f * s),
            t = v * (1 - (1 - f) * s)

        switch (i % 6) {
            case 0:
                r = v
                g = t
                b = p
                break
            case 1:
                r = q
                g = v
                b = p
                break
            case 2:
                r = p
                g = v
                b = t
                break
            case 3:
                r = p
                g = q
                b = v
                break
            case 4:
                r = t
                g = p
                b = v
                break
            case 5:
                r = v
                g = p
                b = q
                break
        }

        return {
            r: Math.round(r * 255),
            g: Math.round(g * 255),
            b: Math.round(b * 255),
            a
        }
    }

    /**
     * 将 RGB 颜色对象转换为 HSV 颜色对象.
     * @param color RGB 颜色对象
     * @returns HSV 颜色对象
     */
    static rgbToHsv (color: RGB): HSV {
        const { r, g, b, a } = color
        const
            max = Math.max(r, g, b),
            min = Math.min(r, g, b),
            d = max - min,
            s = (max === 0 ? 0 : d / max),
            v = max / 255
        let h: number

        switch (max) {
            case min:
                h = 0
                break
            case r:
                h = (g - b) + d * (g < b ? 6 : 0)
                h /= 6 * d
                break
            case g:
                h = (b - r) + d * 2
                h /= 6 * d
                break
            case b:
                h = (r - g) + d * 4
                h /= 6 * d
                break
            default:
                h = 0 // 理论上不会走到这里
        }

        return {
            h: Math.round(h * 360),
            s: Math.round(s * 100),
            v: Math.round(v * 100),
            a
        }
    }

    /**
     * 将颜色文本 (hex 或 rgb/rgba) 转换为 RGB 颜色对象.
     * @param str 颜色字符串
     * @returns RGB 颜色对象
     */
    static textToRgb (str: string): RGB {
        if (typeof str !== 'string') {
            throw new TypeError('Expected a string')
        }

        const color = str.replace(/ /g, '')

        const m = reRGBA.exec(color)

        if (m === null) {
            return this.hexToRgb(color)
        }

        const rgb: RGB = {
            r: Math.min(255, parseInt(m[2], 10)),
            g: Math.min(255, parseInt(m[3], 10)),
            b: Math.min(255, parseInt(m[4], 10))
        }

        if (m[1]) {
            const alpha = parseFloat(m[5])
            // alpha 值在 [0, 1]，需要转换为 [0, 100]
            rgb.a = Math.min(1, isNaN(alpha) === true ? 1 : alpha) * 100
        }

        return rgb
    }

    /**
     * 调亮或调暗颜色.
     * percent > 0 为调亮 (lighten), percent < 0 为调暗 (darken).
     * @param color 颜色字符串
     * @param percent 百分比偏移量, 范围通常为 [-100, 100]
     * @returns 新的十六进制颜色字符串
     */
    static lighten (color: string, percent: number): string {
        if (typeof color !== 'string') {
            throw new TypeError('Expected a string as color')
        }
        if (typeof percent !== 'number') {
            throw new TypeError('Expected a numeric percent')
        }

        const rgb = this.textToRgb(color)
        const
            t = percent < 0 ? 0 : 255,
            p = Math.abs(percent) / 100,
            R = rgb.r,
            G = rgb.g,
            B = rgb.b

        return '#' + (
            0x1000000 + (Math.round((t - R) * p) + R) * 0x10000
            + (Math.round((t - G) * p) + G) * 0x100
            + (Math.round((t - B) * p) + B)
        ).toString(16).slice(1)
    }

    /**
     * 计算颜色的亮度 (Luminosity).
     * @param color 颜色字符串或 RGB 对象
     * @returns 颜色的亮度值 (0 到 1)
     */
    static luminosity (color: string | RGB): number {
        if (typeof color !== 'string' && (!color || color.r === void 0)) {
            throw new TypeError('Expected a string or a {r, g, b} object as color')
        }

        const
            rgb = typeof color === 'string' ? this.textToRgb(color) : color,
            r = rgb.r / 255,
            g = rgb.g / 255,
            b = rgb.b / 255,
            // 标准化 R, G, B 值
            R = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4),
            G = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4),
            B = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4)

        // W3C WCAG 2.0 亮度公式
        return 0.2126 * R + 0.7152 * G + 0.0722 * B
    }

    /**
     * 计算颜色的感知亮度 (Brightness).
     * @param color 颜色字符串或 RGB 对象
     * @returns 颜色的感知亮度值 (0 到 255)
     */
    static brightness (color: string | RGB): number {
        if (typeof color !== 'string' && (!color || color.r === void 0)) {
            throw new TypeError('Expected a string or a {r, g, b} object as color')
        }

        const rgb = typeof color === 'string'
            ? this.textToRgb(color)
            : color

        // 根据人眼感知度的加权平均 (常用公式)
        return (rgb.r * 299 + rgb.g * 587 + rgb.b * 114) / 1000
    }

    /**
     * 将前景颜色 (fgColor) 混合到背景颜色 (bgColor) 上。
     * @param fgColor 前景颜色字符串或 RGB 对象
     * @param bgColor 背景颜色字符串或 RGB 对象
     * @returns 如果 fgColor 是字符串则返回十六进制字符串，否则返回 RGB 对象
     */
    static blend (fgColor: string | RGB, bgColor: string | RGB): string | RGB {
        if (typeof fgColor !== 'string' && (!fgColor || fgColor.r === void 0)) {
            throw new TypeError('Expected a string or a {r, g, b[, a]} object as fgColor')
        }

        if (typeof bgColor !== 'string' && (!bgColor || bgColor.r === void 0)) {
            throw new TypeError('Expected a string or a {r, g, b[, a]} object as bgColor')
        }

        const
            rgb1 = typeof fgColor === 'string' ? this.textToRgb(fgColor) : fgColor,
            r1 = rgb1.r / 255,
            g1 = rgb1.g / 255,
            b1 = rgb1.b / 255,
            a1 = rgb1.a !== void 0 ? rgb1.a / 100 : 1, // 前景 alpha (0-1)
            rgb2 = typeof bgColor === 'string' ? this.textToRgb(bgColor) : bgColor,
            r2 = rgb2.r / 255,
            g2 = rgb2.g / 255,
            b2 = rgb2.b / 255,
            a2 = rgb2.a !== void 0 ? rgb2.a / 100 : 1, // 背景 alpha (0-1)

            // Porter-Duff Over 运算符
            a = a1 + a2 * (1 - a1), // 混合后的总 alpha
            r = Math.round(((r1 * a1 + r2 * a2 * (1 - a1)) / a) * 255),
            g = Math.round(((g1 * a1 + g2 * a2 * (1 - a1)) / a) * 255),
            b = Math.round(((b1 * a1 + b2 * a2 * (1 - a1)) / a) * 255)

        const ret: RGB = { r, g, b, a: Math.round(a * 100) } // a 转换回 (0-100)

        // 如果前景颜色输入是字符串，则返回十六进制字符串，否则返回 RGB 对象
        return typeof fgColor === 'string'
            ? this.rgbToHex(ret)
            : ret
    }

    /**
     * 改变颜色字符串的 alpha 透明度。
     * @param color 颜色字符串 (如 '#RRGGBB' 或 'rgb(r,g,b)')
     * @param offset alpha 的偏移量, 范围 [-1, 1]。正值增加透明度，负值减少透明度。
     * @returns 带有新 alpha 值的十六进制颜色字符串 (#RRGGBBAA)
     */
    static changeAlpha (color: string, offset: number): string {
        if (typeof color !== 'string') {
            throw new TypeError('Expected a string as color')
        }

        if (offset === void 0 || offset < -1 || offset > 1) {
            throw new TypeError('Expected offset to be between -1 and 1')
        }

        const { r, g, b, a } = this.textToRgb(color)
        // 当前 alpha (0-1)
        const alpha = a !== void 0 ? a / 100 : 1 // 如果没有 alpha，默认为 1 (100%)

        // 新的 alpha (0-100)
        const newAlpha = Math.round(Math.min(1, Math.max(0, alpha + offset)) * 100)

        return this.rgbToHex({
            r, g, b, a: newAlpha
        })
    }
}
