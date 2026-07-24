import fs from 'node:fs';
import path from 'node:path';
import {loadEnv} from 'vite';

const VIRTUAL_ID = 'virtual:open-admin/routes';
const RESOLVED_ID = '\0open-admin:routes';

function walk(dir, out = []) {
    if (!fs.existsSync(dir)) return out;
    for (const item of fs.readdirSync(dir)) {
        const full = path.join(dir, item);
        if (fs.statSync(full).isDirectory()) {
            walk(full, out);
        } else {
            out.push(full.split(path.sep).join('/'));
        }
    }
    return out;
}

/** 收集页面文件：业务项目 src/pages 优先，node_modules 中 @jiangood 框架包兜底 */
function collectPageFiles(root) {
    const business = walk(path.resolve(root, 'src/pages')).filter(f => /\.jsx$/.test(f));
    const framework = [];
    const scopeDir = path.resolve(root, 'node_modules/@jiangood');
    if (fs.existsSync(scopeDir)) {
        for (const pkg of fs.readdirSync(scopeDir)) {
            framework.push(...walk(path.join(scopeDir, pkg, 'src/pages')).filter(f => /src\/pages\/.*\.jsx$/.test(f)));
        }
    }
    return [...business, ...framework];
}

/** 页面文件 → 路由路径；大写开头（组件文件）返回 null */
function fileToRoutePath(file) {
    const m = file.match(/src\/pages\/(.+)\.jsx$/);
    if (!m) return null;
    const mainName = m[1].split('/').pop();
    if (mainName.charAt(0) !== mainName.charAt(0).toLowerCase()) return null;
    let routePath = '/' + m[1].replaceAll('$', ':');
    if (routePath.endsWith('/index')) routePath = routePath.substring(0, routePath.length - 6);
    return routePath || '/';
}

/** 生成可被 Vite dev/build 解析的 import 路径 */
function toImportPath(root, file) {
    const nm = file.indexOf('/node_modules/');
    if (nm !== -1) {
        // 依赖框架包：bare specifier，如 @jiangood/open-admin/src/pages/login.jsx
        return file.substring(nm + '/node_modules/'.length);
    }
    // 项目内页面：root 相对，如 /src/pages/login.jsx
    return '/' + path.relative(root, file).split(path.sep).join('/');
}

function generateRoutesModule(root) {
    const seen = new Set();
    const imports = [];
    const entries = [];
    collectPageFiles(root).forEach((file, i) => {
        const routePath = fileToRoutePath(file);
        if (!routePath || seen.has(routePath)) return;
        seen.add(routePath);
        imports.push(`import P${i} from ${JSON.stringify(toImportPath(root, file))};`);
        entries.push(`    {path: ${JSON.stringify(routePath)}, component: P${i}},`);
    });
    return `${imports.join('\n')}\n\nexport default [\n${entries.join('\n')}\n];\n`;
}

export default function openAdmin() {
    let root;
    return {
        name: 'open-admin',

        config(config, {mode, command}) {
            const env = loadEnv(mode, process.cwd(), '');
            const servletContext = env.SERVLET_CONTEXT || '/change-this-servlet-context';
            const serverPort = env.SERVER_PORT || '8080';

            const define = {
                SERVLET_CONTEXT: JSON.stringify(servletContext),
            };
            const theme = {};
            if (env.THEME_PRIMARY_COLOR) theme['primary-color'] = env.THEME_PRIMARY_COLOR;
            if (env.THEME_SUCCESS_COLOR) theme['success-color'] = env.THEME_SUCCESS_COLOR;
            if (env.THEME_WARNING_COLOR) theme['warning-color'] = env.THEME_WARNING_COLOR;
            if (env.THEME_ERROR_COLOR) theme['error-color'] = env.THEME_ERROR_COLOR;
            if (env.THEME_BACKGROUND_COLOR) theme['background-color'] = env.THEME_BACKGROUND_COLOR;
            if (Object.keys(theme).length > 0) {
                define.OPEN_ADMIN_THEME = JSON.stringify(theme);
            }
            if (env.PUBLIC_PAGES) {
                define.OPEN_ADMIN_PUBLIC_PAGES = JSON.stringify(env.PUBLIC_PAGES);
            }

            return {
                base: command === 'build' ? './' : '/',
                define,
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
        },

        configResolved(resolved) {
            root = resolved.root;
        },

        resolveId(id) {
            if (id === VIRTUAL_ID) return RESOLVED_ID;
        },

        load(id) {
            if (id === RESOLVED_ID) return generateRoutesModule(root);
        },

        configureServer(server) {
            const onPagesFileChange = (file) => {
                if (!/src\/pages\/.*\.jsx$/.test(file.split(path.sep).join('/'))) return;
                const mod = server.moduleGraph.getModuleById(RESOLVED_ID);
                if (mod) {
                    server.moduleGraph.invalidateModule(mod);
                    server.ws.send({type: 'full-reload'});
                }
            };
            server.watcher.on('add', onPagesFileChange);
            server.watcher.on('unlink', onPagesFileChange);
        },
    };
}
