import {type ComponentType} from 'react';

function safeDecodeURIComponent(s: string): string {
    try { return decodeURIComponent(s); } catch { return s; }
}

/** 页面布局：admin = 后台菜单布局；blank = 仅渲染页面内容 */
export type PageLayout = 'admin' | 'blank';

/** 页面元信息：页面文件通过 export const pageMeta 声明，由 vite 插件写入路由表 */
export interface PageMeta {
    /** 页面标题：用于页签标题和浏览器标题；未设置时回退到后端菜单名 */
    title?: string;
    /** 是否需要登录，默认 true */
    requiresAuth?: boolean;
    /** 页面布局，默认 admin */
    layout?: PageLayout;
}

export interface RouteDef {
    path: string;
    component: ComponentType<unknown>;
    meta?: PageMeta;
}

interface InternalRoute extends RouteDef {
    segments: ({ param: string } | { name: string })[];
}

export interface MatchResult {
    component: ComponentType<unknown>;
    params: Record<string, string>;
    meta?: PageMeta;
}

const routes: InternalRoute[] = [];

function compile(path: string): InternalRoute['segments'] {
    return path.split('/').filter(Boolean).map(seg =>
        seg.startsWith(':') ? {param: seg.slice(1)} : {name: seg}
    );
}

export function registerRoutes(defs: RouteDef[]): void {
    for (const {path, component, meta} of defs) {
        routes.push({path, component, meta, segments: compile(path)});
    }
}

export function matchRoute(pathname: string): MatchResult | null {
    const segs = pathname.split('/').filter(Boolean);
    let best = null;
    for (const route of routes) {
        if (route.segments.length !== segs.length) continue;
        const params = {};
        let ok = true;
        let dynamic = false;
        for (let i = 0; i < segs.length; i++) {
            const seg = route.segments[i];
            if (seg.param) {
                params[seg.param] = safeDecodeURIComponent(segs[i]);
                dynamic = true;
            } else if (seg.name !== segs[i]) {
                ok = false;
                break;
            }
        }
        if (!ok) continue;
        if (!dynamic) return {component: route.component, params, meta: route.meta};
        if (!best) best = {component: route.component, params, meta: route.meta};
    }
    return best;
}
