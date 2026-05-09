import {ArrayUtils} from "./ArrayUtils";

/**
 * 🌳 通用树节点接口
 * 假设默认的 id, pid, children 字段存在，但支持其他字段
 * @template T 节点数据的类型
 */
export interface TreeNode<T = any> {
    /** 节点的唯一标识 */
    id: string | number;
    /** 父节点的标识 */
    pid: string | number | null;
    /** 子节点数组 */
    children?: TreeNode<T>[];

    /** 节点携带的额外数据 */
    [key: string]: any;
}

/**
 * 🌲 树结构操作工具类
 * 将数组和树结构互相转换，并提供常用的遍历、查找功能。
 */
export class TreeUtils {

    // --- 内部辅助函数 ---

    /**
     * 递归将树转换为列表的实现。
     * @param tree 树节点数组
     * @param level 当前层级
     * @param result 结果列表，用于累积结果
     * @returns 包含层级信息的列表
     */
    private static _treeToList<T extends TreeNode>(tree: T[], level: number, result: (T & { level: number })[]): (T & {
        level: number
    })[] {
        for (const node of tree) {
            // 复制节点并添加 level 字段
            const newNode = {...node, level: level} as T & { level: number };
            result.push(newNode);

            if (node.children && node.children.length > 0) {
                // 递归调用，层级加 1
                this._treeToList(node.children as T[], level + 1, result);
            }
        }
        return result;
    }

// --- 核心工具方法 ---

    /**
     * 🌲 将树结构转换为列表结构 (扁平化)。
     * 节点会被添加一个 `level` 字段表示其层级 (从 1 开始)。
     * @param tree 树节点数组
     * @returns 包含层级信息的扁平化列表
     */
    public static treeToList<T extends TreeNode>(tree: T[]): (T & { level: number }) {
        return TreeUtils._treeToList(tree, 1, []);
    }

    /**
     * 🔎 根据层级查找所有节点的 ID 列表。
     * @param tree 树节点数组
     * @param level 要查找的层级，-1 表示所有层级的 ID。
     * @returns 匹配层级的 ID 数组。
     */
    public static findKeysByLevel<T extends TreeNode>(tree: T[], level: number): (string | number)[] {
        const list = TreeUtils.treeToList(tree);
        // 如果 level 为 -1，则返回所有节点的 id；否则返回匹配 level 的节点 id
        return list
            .filter(t => level === -1 || t.level === level)
            .map(t => t.id);
    }

    /**
     * 🏗️ 将扁平数组转换为树结构。
     * 默认使用 `id` 作为节点标识，`pid` 作为父节点标识。
     * @param list 扁平化节点数组
     * @param idKey 节点 ID 的字段名，默认为 'id'
     * @param pidKey 父节点 ID 的字段名，默认为 'pid'
     * @returns 根节点数组
     */
    public static buildTree<T extends TreeNode>(list: T[], idKey: keyof T | 'id' = 'id', pidKey: keyof T | 'pid' = 'pid'): T[] {
        // 使用 Map 存储所有节点，方便通过 ID 查找
        const map = new Map<string | number, T>();
        list.forEach(node => {
            const id = node[idKey] as string | number;
            map.set(id, node);
        });

        const root: T[] = [];
        for (const node of list) {
            const pid = node[pidKey] as string | number | null | undefined;
            // 通过 pidKey 查找父节点
            const parent = pid ? map.get(pid) : undefined;

            if (parent) {
                // 如果找到父节点，将当前节点挂载到父节点的 children 数组
                if (!parent.children) {
                    parent.children = [];
                }
                // TypeScript 确保 children 数组的类型正确
                (parent.children as T[]).push(node);
            } else {
                // 如果没有父节点 (或找不到)，则视为根节点
                root.push(node);
            }
        }

        return root;
    }

    /**
     * 深度优先遍历树节点。
     * @param tree 树节点数组
     * @param callback 对每个节点执行的回调函数
     */
    public static walk<T extends TreeNode>(tree: T[], callback: (node: T) => void): void {
        if(tree == null){
            return
        }
        for (const node of tree) {
            callback(node); // 执行回调函数

            // 遍历子节点
            if (node.children && node.children.length > 0) {
                TreeUtils.walk(node.children as T[], callback);
            }
        }
    }

    /**
     * 根据键值深度查找单个节点。
     * @param key 要查找的键值 (例如: 节点的 id)
     * @param list 树节点数组
     * @param keyName 要匹配的字段名，默认为 'id'
     * @returns 找到的节点，如果未找到则返回 undefined
     */
    public static findByKey<T extends TreeNode>(key: string | number, list: T[], keyName: keyof T | 'id' = 'id'): T | undefined {
        for (const item of list) {
            // 匹配当前节点
            if (item[keyName] === key) {
                return item;
            }
            // 递归查找子节点
            if (item.children && item.children.length) {
                const rs = TreeUtils.findByKey(key, item.children as T[], keyName);
                if (rs) {
                    return rs;
                }
            }
        }
        return undefined;
    }


    /**
     * 📜 根据键值列表查找所有匹配的节点列表。
     * @param treeData 树节点数组
     * @param keyList 要查找的键值列表
     * @returns 匹配的节点数组
     */
    public static findByKeyList<T extends TreeNode>(treeData: T[], keyList: (string | number)[]): T[] {
        const itemList: T[] = [];

        TreeUtils.walk(treeData, (item) => {
            // 优先使用 key 字段，其次使用 id 字段
            const key = item.key || item.id;

            if (ArrayUtils.contains(keyList, key)) {
                itemList.push(item);
            }
        });

        return itemList;
    }

    /**
     * 获得指定节点下的所有子节点 (扁平化列表，包含自身)。
     * @param treeNode 树节点
     * @param buffer 结果列表，用于递归累积
     * @returns 包含所有子节点的扁平化列表 (包含自身)
     */
    private static _getChild<T extends TreeNode>(treeNode: T, buffer: T[]): T[] {
        if (treeNode.children != null && treeNode.children.length > 0) {
            treeNode.children.forEach((c) => {
                buffer.push(c as T);
                TreeUtils._getChild(c as T, buffer);
            });
        }
        return buffer;
    }

    /**
     * 获得给定根节点列表下的所有节点 (扁平化列表)。
     * @param treeNodeList 树节点数组
     * @returns 包含所有节点的扁平化列表
     */
    public static getSimpleList<T extends TreeNode>(treeNodeList: T[]): T[] {
        const buffer: T[] = [];

        if (treeNodeList != null) {
            treeNodeList.forEach((t) => {
                buffer.push(t);
                TreeUtils._getChild(t, buffer);
            });
        }
        return buffer;
    }


    /**
     * 向上追溯，获取从根节点到指定值节点的完整 Key 路径。
     * 假设节点包含 `id` / `key` 和 `pid` / `parentKey` 字段。
     * @param tree 完整的树结构数组
     * @param value 目标节点的 ID 或 Key
     * @returns 从根节点开始到目标节点的 ID/Key 数组 (包含根和目标节点)
     */
    public static getKeyList<T extends TreeNode>(tree: T[], value: string | number): (string | number)[] {
        const list = TreeUtils.getSimpleList(tree);

        // 使用 Map 快速通过 ID/Key 查找节点
        const map = new Map<string | number, T>();
        list.forEach((t) => {
            const key = t.key || t.id;
            map.set(key, t);
        });

        const targetNode = map.get(value);
        if (targetNode == null) {
            return []; // 未找到目标节点
        }

        const keys: (string | number)[] = [targetNode.key || targetNode.id];
        let parentKey = targetNode.parentKey || targetNode.pid;

        // 向上追溯
        while (parentKey != null) {
            const parent = map.get(parentKey);
            if (parent == null) {
                break; // 找不到父节点，停止追溯
            }
            keys.push(parent.key || parent.id);
            parentKey = parent.parentKey || parent.pid;
        }

        return keys.reverse(); // 从根节点到目标节点的路径
    }
}
