/**
 * 包含了常用的浏览器和网络工具函数。
 */
export class DeviceUtils {

    /**
     * 判断当前设备是否为移动设备 (包括 Windows Phone, Android, iOS)。
     * @returns {boolean} 如果是移动设备返回 true，否则返回 false。
     */
    public static isMobileDevice(): boolean {
        // 使用可选链操作符来安全地访问 navigator 和 window 上的属性，
        // 以防在非浏览器环境中运行 (尽管这个函数主要用于浏览器环境)。
        const userAgent = navigator?.userAgent || navigator?.vendor || (window as any)?.opera || '';

        // 1. Windows Phone 必须放在 Android 之前，因为它的 UA 也可能包含 "Android"
        if (/windows phone/i.test(userAgent)) {
            return true;
        }

        // 2. Android devices
        if (/android/i.test(userAgent)) {
            return true;
        }

        // 3. iOS devices
        // window.MSStream 检查是为了排除旧版 IE 上的触控设备，确保只检测移动设备。
        // 对于 TypeScript，需要断言 window.MSStream 可能不存在。
        if (/iPad|iPhone|iPod/.test(userAgent) && !(window as any).MSStream) {
            return true;
        }

        return false;
    }

    /**
     * 根据当前页面的协议 (http/https) 和主机名构造 WebSocket 的基础 URL (ws/wss)。
     * @returns {string} WebSocket 的基础 URL，例如 "ws://localhost:8080" 或 "wss://example.com"。
     */
    public static getWebsocketBaseUrl(): string {
        // 使用 location.protocol 来确定是 ws 还是 wss。
        const protocol: string = location.protocol === 'http:' ? 'ws:' : 'wss:';

        // location.host 包含主机名和端口号 (如果有)。
        return `${protocol}//${location.host}`;
    }
}
