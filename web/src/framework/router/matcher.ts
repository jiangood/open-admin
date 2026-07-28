import {type ComponentType} from 'react';

function safeDecodeURIComponent(s: string): string {
    try { return decodeURIComponent(s); } catch { return s; }
}

export interface RouteDef {
    path: string;
    component: ComponentType<any>;
}

interface InternalRoute extends RouteDef {
    segments: ({ param: string } | { name: string })[];
}

export interface MatchResult {
    component: ComponentType<any>;
    params: Record<string, string>;
}

const routes: InternalRoute[] = [];

function compile(path: string): InternalRoute['segments'] {
    return path.split('/').filter(Boolean).map(seg =>
        seg.startsWith(':') ? {param: seg.slice(1)} : {name: seg}
    );
}

export function registerRoutes(defs: RouteDef[]): void {
    for (const {path, component} of defs) {
        routes.push({path, component, segments: compile(path)});
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
        if (!dynamic) return {component: route.component, params};
        if (!best) best = {component: route.component, params};
    }
    return best;
}
