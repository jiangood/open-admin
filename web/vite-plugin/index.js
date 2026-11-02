import fs from 'node:fs';
import path from 'node:path';

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

/** 页面文件扩展名：支持 .jsx / .tsx */
const PAGE_EXT_RE = /\.(jsx|tsx)$/;
const PAGE_PATH_RE = /src\/pages\/(.+)\.(jsx|tsx)$/;

/** 收集页面文件：业务项目 src/pages 优先，node_modules 中 @jiangood 框架包兜底 */
function collectPageFiles(root) {
    const business = walk(path.resolve(root, 'src/pages')).filter(f => PAGE_EXT_RE.test(f));
    const framework = [];
    const scopeDir = path.resolve(root, 'node_modules/@jiangood');
    if (fs.existsSync(scopeDir)) {
        for (const pkg of fs.readdirSync(scopeDir)) {
            framework.push(...walk(path.join(scopeDir, pkg, 'src/pages')).filter(f => PAGE_PATH_RE.test(f)));
        }
    }
    return [...business, ...framework];
}

/** 页面文件 → 路由路径；大写开头（组件文件）返回 null */
function fileToRoutePath(file) {
    const m = file.match(PAGE_PATH_RE);
    if (!m) return null;
    const mainName = m[1].split('/').pop();
    if (mainName.charAt(0) !== mainName.charAt(0).toLowerCase()) return null;
    let routePath = '/' + m[1].replaceAll('$', ':');
    if (routePath.endsWith('/index')) routePath = routePath.substring(0, routePath.length - 6);
    return routePath || '/';
}

/** 按页面目录推断 pageMeta 默认值 */
function defaultPageMeta(relPath) {
    const top = relPath.split('/')[0];
    // /login 是框架约定的登录页路径：业务项目覆盖该页面时无需再声明 pageMeta
    if (relPath === 'login' || relPath === 'login/index') return {requiresAuth: false, layout: 'blank'};
    if (top === 'public') return {requiresAuth: false, layout: 'blank'};
    if (top === 'standalone') return {requiresAuth: true, layout: 'blank'};
    return {requiresAuth: true, layout: 'admin'};
}

/** 页面 pageMeta 源码缓存：dev 下只有元信息变化时才重建路由表 */
const pageMetaCache = new Map();

/** 提取 pageMeta 导出源码，用于判断页面元信息是否变化 */
function readPageMetaSource(file) {
    try {
        const source = fs.readFileSync(file, 'utf8');
        const matched = source.match(/export\s+const\s+pageMeta(?:\s*:\s*[^=]+)?\s*=\s*\{[\s\S]*?\};?/);
        return matched ? matched[0].replace(/\s+/g, '') : '';
    } catch {
        return '';
    }
}

/** 生成浏览器端 pageMeta 归一化函数：补默认值、修正非法组合 */
const PAGE_META_HELPER = `function __resolvePageMeta(mod, defaults) {
    let meta = mod?.pageMeta;
    if (meta != null && typeof meta !== 'object') {
        console.warn('[open-admin] 页面 pageMeta 必须是对象，已忽略');
        meta = null;
    }
    const requiresAuth = typeof meta?.requiresAuth === 'boolean' ? meta.requiresAuth : defaults.requiresAuth;
    let layout = meta?.layout === 'admin' || meta?.layout === 'blank' ? meta.layout : defaults.layout;
    if (!requiresAuth && layout === 'admin') {
        console.warn('[open-admin] pageMeta：requiresAuth=false 时必须使用 blank 布局，已自动修正');
        layout = 'blank';
    }
    const title = typeof meta?.title === 'string' && meta.title.trim() ? meta.title.trim() : undefined;
    return {requiresAuth, layout, title};
}`;

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
        const pageMatch = file.match(PAGE_PATH_RE);
        const routePath = fileToRoutePath(file);
        if (!routePath || !pageMatch) return;
        pageMetaCache.set(file, readPageMetaSource(file));
        if (seen.has(routePath)) {
            console.warn(`[open-admin] 路由 ${routePath} 被覆盖（来自 ${file}）`);
            return;
        }
        seen.add(routePath);
        const defaults = defaultPageMeta(pageMatch[1]);
        imports.push(`import P${i}, * as M${i} from ${JSON.stringify(toImportPath(root, file))};`);
        entries.push(`    {path: ${JSON.stringify(routePath)}, component: P${i}, meta: __resolvePageMeta(M${i}, ${JSON.stringify(defaults)})},`);
    });
    return `${imports.join('\n')}\n\n${PAGE_META_HELPER}\n\nexport default [\n${entries.join('\n')}\n];\n`;
}

export default function openAdmin() {
    let root;
    return {
        name: 'open-admin',

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
            const reloadRoutes = () => {
                const mod = server.moduleGraph.getModuleById(RESOLVED_ID);
                if (mod) {
                    server.moduleGraph.invalidateModule(mod);
                    server.ws.send({type: 'full-reload'});
                }
            };
            const onPagesFileChange = (file) => {
                const normalized = file.split(path.sep).join('/');
                if (!PAGE_PATH_RE.test(normalized)) return;
                reloadRoutes();
            };
            // 只有 pageMeta 变化才重建路由表；普通页面代码交由 React Fast Refresh 处理
            const onPageFileChange = (file) => {
                const normalized = file.split(path.sep).join('/');
                if (!PAGE_PATH_RE.test(normalized)) return;
                const next = readPageMetaSource(normalized);
                if (pageMetaCache.get(normalized) === next) return;
                pageMetaCache.set(normalized, next);
                reloadRoutes();
            };
            server.watcher.on('add', onPagesFileChange);
            server.watcher.on('unlink', onPagesFileChange);
            server.watcher.on('change', onPageFileChange);
        },
    };
}
