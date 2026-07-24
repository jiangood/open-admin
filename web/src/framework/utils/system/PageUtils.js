import {history} from '../../router';
import {StringUtils} from "../StringUtils";
import {UrlUtils} from "../UrlUtils";
import {MessageUtils} from "../MessageUtils";

export class PageUtils {
    static redirectToLogin() {
        const url = PageUtils.currentUrl();
        const encodedUrl = encodeURIComponent(url);
        history.push(`/login?redirect=${encodedUrl}`);
    }

    static currentParams() {
        return UrlUtils.getParams();
    }

    static currentPathname() {
        const path = window.location.hash.substring(1);
        return StringUtils.subBefore(path, '?');
    }

    static currentUrl() {
        return window.location.hash.substring(1);
    }

    static currentPathnameLastPart() {
        const path = this.currentPathname();
        return StringUtils.subAfterLast(path, '/') || undefined;
    }

    static open(path, label = '未命名') {
        let targetPath = path;
        if (label) {
            targetPath = UrlUtils.setParam(targetPath, '_label', label);
        }
        history.push(targetPath);
    }

    static openNoLayout(path) {
        const targetPath = UrlUtils.setParam(path, '_noLayout', true);
        history.push(targetPath);
    }

    static currentLabel() {
        return this.currentParams()['_label'];
    }

    static closeCurrent() {
        const event = new CustomEvent('close-page-event', {detail: {url: PageUtils.currentUrl()}});
        document.dispatchEvent(event);
    }

    static closeCurrentAndOpenPage(alertMessage, path, label) {
        MessageUtils.alert(alertMessage).then(() => {
            this.closeCurrent();
            this.open(path, label);
        });
    }
}
