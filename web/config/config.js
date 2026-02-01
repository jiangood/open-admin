import {defineConfig} from 'umi';
import * as fs from "fs";
import * as path from "path";

// https://umijs.org/docs/api/config

const host = '127.0.0.1:8080';

function getPluginDir() {
    try {
        const isFramework = fs.existsSync(path.join(process.cwd(), 'config/common-plugin.js'));
        console.log('isFramework', isFramework)
        return isFramework ? './config' : '@jiangood/open-admin/config';
    } catch (error) {
        console.error('获取插件目录时出错:', error);
        return '@jiangood/open-admin/config';
    }
}

const pluginDir = getPluginDir();

const proxyConfig = {
    '/admin': {
        target: `http://${host}`,
        changeOrigin: true,
    },
    '/preview': {
        target: `http://${host}`,
        changeOrigin: true,
    },
    '/ureport': {
        target: `http://${host}`,
        changeOrigin: true,
        pathRewrite: {'^/ureport': '/ureport'},
    },
    '/admin/ws': {
        target: `ws://${host}`,
        changeOrigin: true,
        ws: true,
    },
};

export default defineConfig({
    // 配置是否让生成的文件包含 hash 后缀，通常用于增量发布和避免浏览器加载缓存
    hash: true,
    // 使用 hash 模式的路由
    history: { type: 'hash' },
    // 暂时关闭 MFSU，避免配置复杂性
    mfsu: false,
    // 启用 esbuild 压缩 IIFE
    esbuildMinifyIIFE: true,
    // 插件配置
    plugins: [
       `${pluginDir}/common-plugin`,
    ],
    // 代理配置
    proxy: proxyConfig,
});
