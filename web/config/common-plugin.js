import * as fs from "fs";
import * as path from "path";
import Utils from './utils.js';

const pkgName = '@jiangood/open-admin';

function convertFileToRoute(file) {
    const absPath = file;
    const mainName = Utils.getFileMainName(file);

    if (mainName.charAt(0) !== mainName.charAt(0).toLowerCase()) {
        return null;
    }

    let routePath = file.substring(file.indexOf('pages') + 6, file.length - 4);
    routePath = routePath.replaceAll("$", ":");

    let parentId = "@@/global-layout";

    if (routePath.endsWith("/index")) {
        routePath = routePath.substring(0, routePath.length - 6);
    }

    return {
        absPath,
        id: routePath,
        path: routePath,
        file,
        parentId
    };
}

export default (api) => {
    api.describe({
        key: 'open-admin',
    });

    // Inject build-time defines and UmiJS defaults from environment variables
    const servletContext = process.env.SERVLET_CONTEXT || '/change-this-servlet-context';
    const isProd = process.env.NODE_ENV === 'production';
    const serverPort = process.env.SERVER_PORT || '8080';

    const theme = {};
    if (process.env.THEME_PRIMARY_COLOR) theme["primary-color"] = process.env.THEME_PRIMARY_COLOR;
    if (process.env.THEME_SUCCESS_COLOR) theme["success-color"] = process.env.THEME_SUCCESS_COLOR;
    if (process.env.THEME_WARNING_COLOR) theme["warning-color"] = process.env.THEME_WARNING_COLOR;
    if (process.env.THEME_ERROR_COLOR) theme["error-color"] = process.env.THEME_ERROR_COLOR;
    if (process.env.THEME_BACKGROUND_COLOR) theme["background-color"] = process.env.THEME_BACKGROUND_COLOR;

    api.modifyConfig((memo) => {
        memo.define = memo.define || {};

        // Define globals
        memo.define.SERVLET_CONTEXT = servletContext;
        if (Object.keys(theme).length > 0) {
            memo.define.OPEN_ADMIN_THEME = theme;
        }

        // Build defaults (only if not explicitly set by project config.js)
        if (memo.publicPath === undefined) {
            memo.publicPath = isProd ? './' : '/';
        }
        if (memo.hash === undefined) memo.hash = true;
        if (memo.history === undefined) memo.history = { type: 'hash' };
        if (memo.mfsu === undefined) memo.mfsu = false;
        if (memo.esbuildMinifyIIFE === undefined) memo.esbuildMinifyIIFE = true;

        // Proxy dev server
        if (memo.proxy === undefined) memo.proxy = {};
        if (!memo.proxy[servletContext]) {
            memo.proxy[servletContext] = {
                target: `http://127.0.0.1:${serverPort}`,
                changeOrigin: true,
            };
        }

        return memo;
    });

    const isFramework = api.pkg.name === pkgName;

    try {
        let frameworkDirs = Utils.getDirs(api.paths.absNodeModulesPath + "/@jiangood");
        frameworkDirs = frameworkDirs.reverse();

        api.logger.info('依赖的框架：', frameworkDirs);

        for (let frameworkDir of frameworkDirs) {
            api.logger.info("正在解析文件夹", frameworkDir);

            const routeFiles = Utils.findFilesSync(frameworkDir, /src\/pages\/.*\.jsx$/);
            api.logger.info("找到的页面文件：", routeFiles);

            api.modifyRoutes((routes) => {
                for (let file of routeFiles) {
                    const route = convertFileToRoute(file);
                    if (route && !routes[route.id]) {
                        api.logger.info("加入路由:", route.id, "路径:", route.absPath);
                        routes[route.id] = route;
                    }
                }
                api.logger.info("路由修改完成");
                return routes;
            });
        }
    } catch (error) {
        api.logger.error('处理路由时出错:', error);
    }

    try {
        let importFrom = path.join(api.paths.absSrcPath, 'framework');
        if (!isFramework) {
            importFrom = path.join(api.paths.absNodeModulesPath, pkgName);
        }

        api.logger.info('formRegistryPath', importFrom);
        api.addEntryImports(() => ({
            source: importFrom,
            specifier: '{FormRegistryUtils}'
        }));

        Utils.findFilesSync(api.paths.absSrcPath, /forms\/.*\.jsx$/).forEach(file => {
            const name = Utils.getFileMainName(file);

            api.addEntryImports(() => ({
                source: file,
                specifier: name
            }));

            api.addEntryCodeAhead(() => `FormRegistryUtils.register("${name}", ${name});`);
            api.logger.info('新版本 formRegistry.register: ', name, file);
        });
    } catch (error) {
        api.logger.error('处理表单时出错:', error);
    }
};
