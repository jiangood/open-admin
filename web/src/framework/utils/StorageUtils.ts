/**
 * 存储工具类
 * 使用 localStorage 进行数据的存取，并包含时间戳和默认值处理。
 */
export class StorageUtils {

    /**
     * 从 localStorage 获取数据。
     * @param key 存储的键。
     * @param defaultValue 如果找不到键或值，返回的默认值。
     * @returns 存储的值或默认值。
     */
    static get<T>(key: string, defaultValue: T | null = null): T | null {
        const vStr = localStorage.getItem(key);
        if (vStr == null) {
            return defaultValue;
        }

        try {
            return JSON.parse(vStr);
        } catch (error) {
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
        if (value == null) {
            localStorage.removeItem(key);
            return;
        }

        try {
            const dataStr = JSON.stringify(value);
            localStorage.setItem(key, dataStr);
        } catch (error) {
            console.error(`Error stringify storage value for key: ${key}`, error);
        }
    }
}
