import {history} from '../router';
import {StringUtils} from "./StringUtils";
import {UrlUtils} from "./UrlUtils";
import {EventBus} from "./EventBus";

export class PageUtils {
    static redirectToLogin(): void {
        if (PageUtils.currentPathname() === '/public/login') {
            return;
        }
        const url = PageUtils.currentUrl();
        const encodedUrl = encodeURIComponent(url);
        history.push(`/public/login?redirect=${encodedUrl}`);
    }

    static currentParams(): Record<string, string | undefined> {
        return UrlUtils.getParams();
    }

    static currentPathname(): string {
        const path = window.location.hash.substring(1);
        return StringUtils.subBefore(path, '?');
    }

    static currentUrl(): string {
        return window.location.hash.substring(1);
    }

    static currentPathnameLastPart(): string | undefined {
        const path = this.currentPathname();
        return StringUtils.subAfterLast(path, '/') || undefined;
    }

    static open(path: string, label = '未命名'): void {
        let targetPath = path;
        if (label) {
            targetPath = UrlUtils.setParam(targetPath, '_label', label);
        }
        history.push(targetPath);
    }

    static currentLabel(): string | undefined {
        return this.currentParams()['_label'];
    }

    static closeCurrent(): void {
        EventBus.emit('closePage', PageUtils.currentUrl());
    }
}
