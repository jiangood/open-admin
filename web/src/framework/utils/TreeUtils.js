export class TreeUtils {
    static walk(tree, callback) {
        if (tree == null) return;
        for (const node of tree) {
            callback(node);
            if (node.children && node.children.length > 0) {
                TreeUtils.walk(node.children, callback);
            }
        }
    }

    static findByKey(key, list, keyName = 'id') {
        for (const item of list) {
            if (item[keyName] === key) return item;
            if (item.children && item.children.length) {
                const rs = TreeUtils.findByKey(key, item.children, keyName);
                if (rs) return rs;
            }
        }
        return undefined;
    }

    static _getChild(treeNode, buffer) {
        if (treeNode.children != null && treeNode.children.length > 0) {
            treeNode.children.forEach((c) => {
                buffer.push(c);
                TreeUtils._getChild(c, buffer);
            });
        }
        return buffer;
    }

    static flattenTree(treeNodeList) {
        const buffer = [];
        if (treeNodeList != null) {
            treeNodeList.forEach((t) => {
                buffer.push(t);
                TreeUtils._getChild(t, buffer);
            });
        }
        return buffer;
    }

    static getKeyList(tree, value) {
        const list = TreeUtils.flattenTree(tree);
        const map = new Map();
        list.forEach((t) => {
            const key = t.key || t.id;
            map.set(key, t);
        });
        const targetNode = map.get(value);
        if (targetNode == null) return [];
        const keys = [targetNode.key || targetNode.id];
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
