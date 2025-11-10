const IApi = require( 'umi').IApi;

const fs = require("fs");
const path = require("path");

// 扫描依赖模块的路由

function addRoute(file, fileRoutes) {
    let routePath = file.substring(file.indexOf('pages') + 6, file.length - 4)
    routePath = routePath.replaceAll('\\', '/')

    let parentId = "@@/global-layout";

    // 文件$开头的会替换为路径变量 如$id 变为 :id
    routePath = routePath.replaceAll("\$", ":")

    fileRoutes.push({
        absPath: file,
        id: routePath,
        path: routePath,
        file,
        parentId
    })
    if (routePath.endsWith("/index")) {
        routePath = routePath.substring(0, routePath.length - 6)
        fileRoutes.push({
            absPath: file,
            id: routePath,
            path: routePath,
            file,
            parentId: parentId
        })
    }
}



module.exports = (api) => {
    api.describe({
        key: 'addDependenceRoutes',
    });

    const content = fs.readFileSync(api.cwd + "/package.json", "utf-8")
    const pkg = JSON.parse(content)
    const deps = Object.assign({}, pkg.devDependencies, pkg.dependencies, pkg.peerDependencies)
    const pagePkg = [];
    for (let k in deps) {
        if (k.startsWith("@tmgg/tmgg-system")) {
            pagePkg.push(k)
        }
    }

    const fileRoutes = []
    for (let pkg of pagePkg) {
        const pageDir = api.cwd + "/node_modules/" + pkg + "/src/pages"
        parseDir(pageDir, fileRoutes)
    }
    api.modifyRoutes((routes) => {
        for (let fileRoute of fileRoutes) {
            if(routes[fileRoute.id] == null){
                routes[fileRoute.id] = fileRoute
            }
        }
        return routes;
    })
};
function parseDir(pageDir, fileRoutes) {
    const list = fs.readdirSync(pageDir)

    for (let fileName of list) {
        const fullPath = path.join(pageDir, fileName)
        const stats = fs.statSync(fullPath)
        if (stats.isFile()) {
            if (fileName.endsWith(".jsx")) {
                addRoute(fullPath, fileRoutes)
            }
        } else if (stats.isDirectory()) {
            parseDir(fullPath, fileRoutes)
        }

    }
}
