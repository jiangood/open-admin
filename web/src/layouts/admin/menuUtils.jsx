import React from 'react';
import {NamedIcon} from "../../framework";

/** 顶部菜单模式下，根级叶子菜单归入的虚拟一级菜单 key（使用保留前缀避免与真实菜单 id 冲突） */
export const DEFAULT_TOP_MENU_KEY = '__oa_default_group__';

/** 顶部菜单模式下默认分组的名称 */
export const DEFAULT_TOP_MENU_LABEL = '业务模块';

/** 目录节点：type 为 directory，或存在子节点 */
export function isDirectoryNode(node) {
    return node.type === 'directory' || (node.children && node.children.length > 0);
}

/**
 * 将完整菜单树分类为顶部一级菜单。
 *
 * - 根级目录直接作为一级菜单；
 * - 根级叶子菜单归入虚拟的「业务模块」目录，避免顶栏一级菜单过多；
 * - 根级分隔线忽略（进入虚拟目录会丢失分组语义）。
 */
export function classifyMenus(menuTree) {
    const topMenus = [];
    const leafRootNodes = [];

    (menuTree || []).forEach(node => {
        if (node.type === 'divider') {
            return;
        }
        if (isDirectoryNode(node)) {
            topMenus.push(node);
        } else {
            leafRootNodes.push(node);
        }
    });

    if (leafRootNodes.length > 0) {
        topMenus.unshift({
            key: DEFAULT_TOP_MENU_KEY,
            label: DEFAULT_TOP_MENU_LABEL,
            icon: <NamedIcon name="AppstoreOutlined" style={{fontSize: 16}}/>,
            children: leafRootNodes,
            type: 'directory',
        });
    }

    return topMenus;
}

/**
 * 从菜单定义查找其所属的一级菜单。
 *
 * 依次尝试：自身就是一级菜单 → 直接挂在某个一级菜单下（含虚拟「业务模块」）→ 沿 pid 链向上。
 */
export function findActiveTopMenu(topMenus, menuDef, menuMap) {
    if (!menuDef) return null;
    const menus = topMenus || [];

    const self = menus.find(t => t.key === menuDef.id);
    if (self) return self;

    const direct = menus.find(t => (t.children || []).some(child => child.key === menuDef.id));
    if (direct) return direct;

    let pid = menuDef.pid || menuDef.parentKey;
    while (pid) {
        const match = menus.find(t => t.key === pid);
        if (match) return match;
        const parent = menuMap[pid];
        pid = parent ? (parent.pid || parent.parentKey) : null;
    }
    return null;
}

/** 查找某个菜单项的所有祖先 key；未找到返回 null，自身是根节点返回 [] */
export function findAncestorKeys(nodes, key, chain = []) {
    for (const node of nodes || []) {
        if (node.key === key) {
            return chain;
        }
        const found = findAncestorKeys(node.children, key, [...chain, node.key]);
        if (found) {
            return found;
        }
    }
    return null;
}

/** 菜单项是否在给定节点集合中（直接或嵌套） */
export function containsMenuKey(nodes, key) {
    return findAncestorKeys(nodes, key) !== null;
}

/** 展开某个菜单项所属的各级目录，同时保留用户已展开的其它目录 */
export function expandForMenu(nodes, openKeys, key) {
    const ancestors = findAncestorKeys(nodes, key) || [];
    return Array.from(new Set([...(openKeys || []), ...ancestors]));
}
