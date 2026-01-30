import {defineConfig} from 'umi';
import * as fs from "fs";
import * as path from "path";

//https://umijs.org/docs/api/config

console.log('i am config.ts')

console.log('__dirname', __dirname)
console.log('process.cwd()',process.cwd())

const isFramework = fs.existsSync(path.join(process.cwd(), 'config/common-plugin.ts'));
console.log('isFramework', isFramework)
const pluginDir = isFramework ? './config' : '@jiangood/open-admin/config/dist';
console.log('pluginDir', pluginDir)

const host = '127.0.0.1:8080';
export default defineConfig({
    // 配置是否让生成的文件包含 hash 后缀，通常用于增量发布和避免浏览器加载缓存。
    hash: true,
    history: {type: 'hash'},
    // 复杂，还得设置忽略、编译等，先关掉
    mfsu: false,
    esbuildMinifyIIFE: true,



    plugins: [
        pluginDir +'/common-plugin',
     ],

    proxy: {
        '/admin': {
            target: 'http://' + host,
            changeOrigin: true,
        },
        '/preview': {
            target: 'http://'+host,
            changeOrigin: true,
        },

        '/ureport': {
            target: 'http://' + host,
            changeOrigin: true,
            pathRewrite: {'^/ureport': '/ureport'},
        },
        '/admin/ws': {
            target: 'ws://' +  host,
            changeOrigin: true,
            ws: true,
        },
    }
});


