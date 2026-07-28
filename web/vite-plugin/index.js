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
        if (!routePath) return;
        if (seen.has(routePath)) {
            console.warn(`[open-admin] 路由 ${routePath} 被覆盖（来自 ${file}）`);
            return;
        }
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
                if (!PAGE_PATH_RE.test(file.split(path.sep).join('/'))) return;
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
