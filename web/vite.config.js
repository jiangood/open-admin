import {defineConfig, loadEnv} from 'vite';
import react from '@vitejs/plugin-react';
import openAdmin from './vite-plugin/index.js';

export default defineConfig(({mode, command}) => {
    const env = loadEnv(mode, process.cwd(), '');
    const servletContext = env.VITE_SERVER_SERVLET_CONTEXT_PATH;
    const serverPort = env.SERVER_PORT ;
    const port = Number(env.PORT);
    console.log('前端端口' + port +',后端端口' + serverPort +',请求上下文' + servletContext)

    return {
        plugins: [react(), openAdmin()],
        base: command === 'build' ? './' : '/',
        server: {
            host: '0.0.0.0',
            port: port,
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