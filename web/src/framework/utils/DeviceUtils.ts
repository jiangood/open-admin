declare global {
    interface Window {
        opera?: string;
        MSStream?: unknown;
    }
}

export class DeviceUtils {

    static isMobileDevice(): boolean {
        const userAgent = navigator?.userAgent || navigator?.vendor || window?.opera || '';
        if (/windows phone/i.test(userAgent)) return true;
        if (/android/i.test(userAgent)) return true;
        if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) return true;
        return false;
    }
}
