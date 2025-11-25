import {IApi} from 'umi';
import * as fs from "fs";
import * as path from "path";

// 自动注册src/forms下的表单
const pkgName = '@jiangood/springboot-admin-starter';


export default (api: IApi) => {
    api.logger.info('plugin starting... ')
    api.logger.info('info', JSON.stringify(api.env))

    api.describe({
        key: 'form-register-by-dir',
    });
    const isFramework = api.pkg.name === pkgName;
    api.logger.info('current pkgName', api.pkg.name)
    api.logger.info('is framework ?', isFramework)

    let formRegistryPath = path.join(api.paths.absSrcPath, 'framework')
    if (!isFramework) {
        formRegistryPath = path.join(api.paths.absNodeModulesPath, pkgName)
    }
    api.logger.info('formRegistryPath', formRegistryPath)
    api.addEntryImports(() => ({
        source: formRegistryPath,
        specifier: '{FormRegistryUtils}'
    }))

    parseDir(api, path.join(api.paths.absSrcPath, 'forms'))
    if (!isFramework) {
        parseDir(api, path.join(api.paths.absNodeModulesPath, pkgName, 'src', 'forms'))
    }

};

function parseDir(api: IApi, dir: string) {
    api.logger.info('scan dir', dir)
    if (!fs.existsSync(dir)) {
        api.logger.info('dir not exist, return ')
        return
    }
    fs.readdirSync(dir).forEach(file => {
        if (!file.endsWith(".jsx")) {
            return;
        }
        let source = path.join(dir, file);
        let name = file.replace(".jsx", "");


        // import form
        api.addEntryImports(() => ({
            source: source,
            specifier: name
        }))

        // register form
        api.addEntryCodeAhead(() => `FormRegistryUtils.register("${name}",${name} );`)
        api.logger.info('formRegistry.register: ', name, source)
    });
}
