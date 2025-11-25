import { DateUtils } from "./DateUtils";

// 如果 ENCRYPT 仅在当前文件内使用，且不需要作为公共属性，可以保持为局部常量
const ENCRYPT = false;

/**
 * 存储工具类
 * 使用 localStorage 进行数据的存取，并包含时间戳和默认值处理。
 */
export class StorageUtils {

    /**
     * 从 localStorage 获取数据。
     * @param key 存储的键。
     * @param defaultValue 如果找不到键或值，返回的默认值 (默认为 null)。
     * @returns 存储的值或默认值。
     */
    static get<T>(key: string, defaultValue: T | null = null): T | null {
        // 尝试从 localStorage 获取原始字符串
        const vStr = localStorage.getItem(key);

        // 如果没有存储项
        if (vStr == null) {
            return defaultValue;
        }

        try {
            // 解析存储的项
            const item: { createTime: number; data: T } = JSON.parse(vStr);

            // 返回实际存储的数据
            return item.data;

        } catch (error) {
            // 如果 JSON 解析失败 (例如，存储的值不是一个包含 createTime 和 data 的有效 JSON 结构)
            console.error(`Error parsing storage item for key: ${key}`, error);
            return defaultValue;
        }
    }

    /**
     * 将数据存储到 localStorage。
     * 如果值为 null 或 undefined，则删除该键。
     * @param key 存储的键。
     * @param value 要存储的值。
     */
    static set<T>(key: string, value: T | null | undefined): void {
        // 如果值为 null 或 undefined，则删除该键
        if (value == null) {
            localStorage.removeItem(key);
            return;
        }

        // 构造包含时间戳的对象
        const item = {
            createTime: DateUtils.now(), // 假设 DateUtil.now() 返回 number 类型的时间戳
            data: value
        };

        try {
            // 序列化对象
            const dataStr = JSON.stringify(item);
            // 存储到 localStorage
            localStorage.setItem(key, dataStr);
        } catch (error) {
            // 捕获 JSON.stringify 错误 (例如，如果 value 包含循环引用)
            console.error(`Error stringifying storage value for key: ${key}`, error);
        }
    }
}
