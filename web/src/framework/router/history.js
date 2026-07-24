function currentUrl() {
    return window.location.hash.substring(1) || '/';
}

function parseLocation() {
    const url = currentUrl();
    const qIndex = url.indexOf('?');
    if (qIndex === -1) {
        return {pathname: url, search: ''};
    }
    return {pathname: url.substring(0, qIndex), search: url.substring(qIndex)};
}

const listeners = new Set();
let lastNotifiedUrl = currentUrl();

function notify() {
    lastNotifiedUrl = currentUrl();
    const location = parseLocation();
    history.location = location;
    listeners.forEach(fn => fn({location}));
}

window.addEventListener('hashchange', () => {
    // push/replace 已同步通知；此处仅处理浏览器前进/后退或手动修改 hash
    if (currentUrl() === lastNotifiedUrl) return;
    notify();
});

export const history = {
    location: parseLocation(),

    push(url) {
        if (url === currentUrl()) return;
        window.location.hash = url;
        notify();
    },

    replace(url) {
        if (url === currentUrl()) return;
        const base = window.location.href.split('#')[0];
        window.location.replace(base + '#' + url);
        notify();
    },

    listen(fn) {
        listeners.add(fn);
        return () => listeners.delete(fn);
    },
};
