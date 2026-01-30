import {IApi} from 'umi';
import * as fs from "fs";
import * as path from "path";

const pkgName = '@jiangood/open-admin';


export default (api: IApi) => {

    api.describe({
        key: 'admin-spring-boot-stater',
    });

    api.logger.info('plugin starting... ')

    const isFramework = api.pkg.name === pkgName;



    const frameworkDirs = Utils.getDirs(api.paths.absNodeModulesPath + "/@jiangood");
    api.logger.info('依赖的框架：', frameworkDirs)

    // ==================== 处理路由 ========================
    for (let frameworkDir of frameworkDirs) {
        api.logger.info("正在解析文件夹", frameworkDir)

        // 处理路由
        const routeFiles = Utils.findFilesSync(frameworkDir, /src\/pages\/.*\.jsx$/) // src/pages/**
        api.logger.info("找到的页面文件：", routeFiles)
        api.modifyRoutes((routes) => {
            for (let file of routeFiles) {
                const route = convertFileToRoute(file)
                if (route) {
                    if (routes[route.id] == null) { //  如果用户项目没有这个路由，则加入。否则以用户项目为准
                        api.logger.info("加入路由:", route.id, "路径:", route.absPath)
                        routes[route.id] = route
                    }
                }
            }
            api.logger.info("路由修改完成")
            return routes;
        })
    }


    // =============== 处理表单 ========================

    {// 导入工具类 FormRegistryUtils
        let importFrom = path.join(api.paths.absSrcPath, 'framework')
        if (!isFramework) {
            importFrom = path.join(api.paths.absNodeModulesPath, pkgName)
        }

        api.logger.info('formRegistryPath', importFrom)
        api.addEntryImports(() => ({
            source: importFrom,
            specifier: '{FormRegistryUtils}'
        }))
    }


    Utils.findFilesSync(api.paths.absSrcPath, /forms\/.*\.jsx$/).forEach(file => {
        const name = Utils.getFileMainName(file)

        api.addEntryImports(() => ({
            source: file,
            specifier: name
        }))

        // register form
        api.addEntryCodeAhead(() => `FormRegistryUtils.register("${name}",${name} );`)
        api.logger.info('新版本 formRegistry.register: ', name, file)
    })

};


function convertFileToRoute(file) {
    const absPath = file;
    const mainName = Utils.getFileMainName(file)

    // 只添加小写开头的文件页面，大写的默认为组件
    if (mainName.charAt(0) !== mainName.charAt(0).toLowerCase()) {
        return
    }

    let routePath = file.substring(file.indexOf('pages') + 6, file.length - 4)
    routePath = routePath.replaceAll("\$", ":")    // 文件$开头的会替换为路径变量 如$id 变为 :id

    let parentId = "@@/global-layout";

    if (routePath.endsWith("/index")) {
        routePath = routePath.substring(0, routePath.length - 6)
    }
    return {
        absPath,
        id: routePath,
        path: routePath,
        file,
        parentId
    }
}

class Utils {

    /**
     * 获取目录下的所有子目录
     * @param dir
     * @returns {string[]}  完整路径
     */
    static getDirs(dir) {
        console.log('获取dir....', dir)

        if (!fs.existsSync(dir)) throw new Error(`目录不存在: ${dir}`);


        return fs.readdirSync(dir).map(item => {
            const fullPath = path.join(dir, item);
            if (fs.statSync(fullPath).isDirectory()) {
                return path.join(dir, item)
            }
        });
    }

    /**
     * 同步获取目录下所有文件
     */
    static getAllFilesSync(dir) {
        if (!fs.existsSync(dir)) throw new Error(`目录不存在: ${dir}`);

        const results = [];

        function traverse(currentDir) {
            fs.readdirSync(currentDir).forEach(item => {
                const fullPath = path.join(currentDir, item);
                if (fs.statSync(fullPath).isDirectory()) {
                    traverse(fullPath);
                } else {
                    results.push(fullPath.split(path.sep).join('/'));
                }
            });
        }

        traverse(dir);


        return results;
    }

    /**
     * 同步查找匹配模式的文件
     *
     * @return 文件列表 全路径
     */
    static findFilesSync(dir, pattern: RegExp) {
        const files = this.getAllFilesSync(dir);

        return files.filter(file => pattern.test(file)).map(file => file.split(path.sep).join('/'))
    }

    /**
     * 获取文件名（不含后缀）
     * @param {string} filePath - 文件路径
     * @returns {string} 文件名（不含后缀）
     */
    public static getFileMainName(filePath) {
        // 从路径中提取文件名
        const fileName = path.basename(filePath);

        // 移除扩展名
        const lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName; // 没有扩展名的情况
    }
}