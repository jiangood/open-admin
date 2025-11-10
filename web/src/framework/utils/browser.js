export function isMobileDevice() {
    const userAgent = navigator.userAgent || navigator.vendor || window.opera;

    // Windows Phone must come first because its UA also contains "Android"
    if (/windows phone/i.test(userAgent)) {
        return true;
    }

    // Android devices
    if (/android/i.test(userAgent)) {
        return true;
    }

    // iOS devices
    if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
        return true;
    }
    return false;
}

export function getWebsocketBaseUrl() {
    const protocol = location.protocol === 'http:' ? 'ws:' : 'wss:'
    return protocol + "//" + location.host
}
