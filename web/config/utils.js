import * as fs from "fs";
import * as path from "path";

class Utils {
    /**
     * 获取目录下的所有子目录
     * @param dir
     * @returns {string[]}  完整路径
     */
    static getDirs(dir) {
        if (!fs.existsSync(dir)) {
            return [];
        }

        return fs.readdirSync(dir)
            .map(item => {
                const fullPath = path.join(dir, item);
                if (fs.statSync(fullPath).isDirectory()) {
                    return path.join(dir, item);
                }
            })
            .filter(Boolean);
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
    static findFilesSync(dir, pattern) {
        const files = this.getAllFilesSync(dir);
        return files.filter(file => pattern.test(file));
    }

    /**
     * 获取文件名（不含后缀）
     * @param {string} filePath - 文件路径
     * @returns {string} 文件名（不含后缀）
     */
    static getFileMainName(filePath) {
        const fileName = path.basename(filePath);
        const lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}

export default Utils;
