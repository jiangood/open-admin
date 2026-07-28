export interface TreeNode<T = any> {
    id: string | number;
    pid: string | number | null;
    children?: TreeNode<T>[];
    [key: string]: any;
}

export class TreeUtils {
    static walk<T extends TreeNode>(tree: T[], callback: (node: T) => void): void {
        if (tree == null) return;
        for (const node of tree) {
            callback(node);
            if (node.children && node.children.length > 0) {
                TreeUtils.walk(node.children as T[], callback);
            }
        }
    }

    static findByKey<T extends TreeNode>(key: string | number, list: T[], keyName: keyof T | 'id' = 'id'): T | undefined {
        for (const item of list) {
            if (item[keyName as keyof T] === key) return item;
            if (item.children && item.children.length) {
                const rs = TreeUtils.findByKey(key, item.children as T[], keyName);
                if (rs) return rs;
            }
        }
        return undefined;
    }

    /** @private */
    static getChildRecursive<T extends TreeNode>(treeNode: T, buffer: T[]): T[] {
        if (treeNode.children != null && treeNode.children.length > 0) {
            (treeNode.children as T[]).forEach((c) => {
                buffer.push(c);
                TreeUtils.getChildRecursive(c, buffer);
            });
        }
        return buffer;
    }

    static flattenTree<T extends TreeNode>(treeNodeList: T[]): T[] {
        const buffer: T[] = [];
        if (treeNodeList != null) {
            treeNodeList.forEach((t) => {
                buffer.push(t);
                TreeUtils.getChildRecursive(t, buffer);
            });
        }
        return buffer;
    }

    static getKeyList<T extends TreeNode>(tree: T[], value: string | number): (string | number)[] {
        const list = TreeUtils.flattenTree(tree);
        const map = new Map<string | number, T>();
        list.forEach((t) => {
            const key = t.key || t.id;
            map.set(key, t);
        });
        const targetNode = map.get(value);
        if (targetNode == null) return [];
        const keys: (string | number)[] = [targetNode.key || targetNode.id];
        let parentKey = targetNode.parentKey || targetNode.pid;
        while (parentKey != null) {
            const parent = map.get(parentKey);
            if (parent == null) break;
            keys.push(parent.key || parent.id);
            parentKey = parent.parentKey || parent.pid;
        }
        return keys.reverse();
    }
}
