import {defineConfig, loadEnv} from 'vite';
import react from '@vitejs/plugin-react';
import openAdmin from './vite-plugin/index.js';

export default defineConfig(({mode, command}) => {
    const env = loadEnv(mode, process.cwd(), '');
    const servletContext = env.VITE_SERVLET_CONTEXT || '/change-this-servlet-context';
    const serverPort = env.SERVER_PORT || '8080';

    return {
        plugins: [react(), openAdmin()],
        base: command === 'build' ? './' : '/',
        optimizeDeps: {exclude: ['@jiangood/open-admin']},
        server: {
            port: env.PORT ? Number(env.PORT) : undefined,
            proxy: {
                [servletContext]: {
                    target: `http://127.0.0.1:${serverPort}`,
                    changeOrigin: true,
                    ws: true,
                },
            },
        },
    };
});