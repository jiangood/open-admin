
// --- 常量定义 ---
import {StorageUtils} from "../StorageUtils";

const SITE_INFO_KEY = "siteInfo";
const LOGIN_INFO_KEY = "loginInfo";
const DICT_INFO_KEY = "dictInfo";

// 假设 SiteInfo, LoginInfo, DictInfo 是工具类操作的数据结构。
// 请根据你的实际情况替换为正确的接口或类型。
interface SiteInfo {
    // 示例属性
    name: string;
    version: string;
}

interface LoginInfo {
    // 示例属性
    token: string;
    userId: number;
}

// 字典数据通常是键值对或数组，这里假设是 Record<string, any>
type DictInfo = Record<string, any> | null;

/**
 * 系统工具类 (Utils Pattern)
 */
export class SysUtils {

    /**
     * 获取请求头信息。
     * @returns 一个包含请求头键值对的对象。
     */
    public static getHeaders(): Record<string, string> {
        const headers: Record<string, string> = {};
        // 实际的 headers 逻辑（如添加认证 token 等）应在此处实现
        return headers;
    }

    /**
     * 设置站点信息。
     * @param data 要存储的站点信息数据。
     * @returns StorageUtil.set 的返回值 (通常是 void 或 Promise<void>)
     */
    public static setSiteInfo(data: SiteInfo): void {
        // 假设 StorageUtil.set 返回 void
        // 请根据实际 StorageUtil 的实现调整返回值类型
        StorageUtils.set(SITE_INFO_KEY, data);
    }

    /**
     * 获取站点信息。
     * @returns 站点信息对象，如果不存在则返回一个空对象。
     */
    public static getSiteInfo(): SiteInfo {
        // 假设 StorageUtil.getDefinition 内部处理了 JSON.parse，并返回对应类型或 null
        return StorageUtils.get(SITE_INFO_KEY) || {} as SiteInfo;
    }

    /**
     * 设置登录信息。
     * @param data 要存储的登录信息数据。
     */
    public static setLoginInfo(data: LoginInfo): void {
        StorageUtils.set(LOGIN_INFO_KEY, data);
    }

    /**
     * 获取登录信息。
     * @returns 登录信息对象，如果不存在则返回一个空对象。
     */
    public static getLoginInfo(): LoginInfo {
        return StorageUtils.get(LOGIN_INFO_KEY) || {} as LoginInfo;
    }

    /**
     * 设置字典信息。
     * @param data 要存储的字典信息数据。
     */
    public static setDictInfo(data: DictInfo): void {
        StorageUtils.set(DICT_INFO_KEY, data);
    }

    /**
     * 获取字典信息。
     * @returns 字典信息对象，如果不存在则返回 null。
     */
    public static getDictInfo(): DictInfo {
        return StorageUtils.get(DICT_INFO_KEY);
    }
}

// 导出常量（保持不变，但通常将常量放在类内部作为 static readonly 属性更符合 class 模式，
// 不过为了兼容性，我先保留外部导出，同时建议在类中使用常量）

export { SITE_INFO_KEY, LOGIN_INFO_KEY, DICT_INFO_KEY };
