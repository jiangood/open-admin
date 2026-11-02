import {history} from '../router';
import {StringUtils} from "./StringUtils";
import {UrlUtils} from "./UrlUtils";
import {EventBus} from "./EventBus";
import {getPageTitle} from "./TitleUtils";

/** 登录页路径：公开页面，支持 ?redirect= 回跳 */
export const LOGIN_PATH = '/login';

let labelDeprecatedWarned = false;

/**
 * @internal
 * `_label` 参数已弃用：首次被使用时提示一次，方便业务项目迁移
 */
export function warnDeprecatedPageLabel(): void {
    if (labelDeprecatedWarned) return;
    labelDeprecatedWarned = true;
    console.warn(
        '[open-admin] 检测到已弃用的 _label 参数：该参数已不再生效，请改用页面文件中的 pageMeta.title。'
    );
}

export class PageUtils {
    static redirectToLogin(): void {
        if (PageUtils.currentPathname() === LOGIN_PATH) {
            return;
        }
        const url = PageUtils.currentUrl();
        const encodedUrl = encodeURIComponent(url);
        history.push(`${LOGIN_PATH}?redirect=${encodedUrl}`);
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

    /**
     * 打开页面并切换到对应 Tab
     * @param path 页面路径
     * @param label @deprecated 已不再生效，仅用于提示迁移；页面标题请使用 pageMeta.title
     */
    static open(path: string, label?: string): void {
        if (label) {
            warnDeprecatedPageLabel();
        }
        history.push(path);
    }

    /**
     * @deprecated `_label` 已不再生效，页面标题请使用 pageMeta.title 或 {@link PageUtils.currentTitle}
     */
    static currentLabel(): string | undefined {
        warnDeprecatedPageLabel();
        return undefined;
    }

    /** 获取当前页面标题（不含系统标题）；没有页面标题时返回 undefined */
    static currentTitle(): string | undefined {
        return getPageTitle();
    }

    static closeCurrent(): void {
        EventBus.emit('closePage', PageUtils.currentUrl());
    }
}
