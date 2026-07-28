/// <reference types="vite/client" />

declare module 'virtual:open-admin/routes' {
    import type {ComponentType} from 'react';
    const routes: { path: string; component: ComponentType<any> }[];
    export default routes;
}