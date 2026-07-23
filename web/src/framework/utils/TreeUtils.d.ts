export interface TreeNode<T = any> {
    id: string | number;
    pid: string | number | null;
    children?: TreeNode<T>[];
    [key: string]: any;
}

export class TreeUtils {
    static walk<T extends TreeNode>(tree: T[], callback: (node: T) => void): void;
    static findByKey<T extends TreeNode>(key: string | number, list: T[], keyName?: keyof T | 'id'): T | undefined;
    static flattenTree<T extends TreeNode>(treeNodeList: T[]): T[];
    static getKeyList<T extends TreeNode>(tree: T[], value: string | number): (string | number)[];
}
