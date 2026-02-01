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
