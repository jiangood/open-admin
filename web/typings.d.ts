/// <reference types="vite/client" />

declare module 'virtual:open-admin/routes' {
    import type {ComponentType} from 'react';
    import type {PageMeta} from './src/framework/router/matcher';
    const routes: { path: string; component: ComponentType<any>; meta?: PageMeta }[];
    export default routes;
}