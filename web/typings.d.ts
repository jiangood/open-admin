declare const SERVLET_CONTEXT: string;
declare const OPEN_ADMIN_THEME: Record<string, string> | undefined;
declare const OPEN_ADMIN_PUBLIC_PAGES: string | undefined;

declare module 'virtual:open-admin/routes' {
    import type {ComponentType} from 'react';
    const routes: { path: string; component: ComponentType<any> }[];
    export default routes;
}