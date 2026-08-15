package io.github.jiangood.openadmin.util.tree;


import cn.hutool.core.collection.CollUtil;
import io.github.jiangood.openadmin.util.dto.TreeOption;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 树工具类，提供树构建、遍历、查询等静态方法
 */
public class TreeTool {
    private TreeTool() {
    }


    /**
     * 判断节点是否为叶子
     */
    public static <E> boolean isLeaf(E node, Function<E, List<E>> getChildren) {
        return node == null || CollUtil.isEmpty(getChildren.apply(node));
    }

    /**
     * 从平铺列表中获取某个节点的所有子孙节点
     */
    public static <E> List<E> getAllChildren(List<E> list, String id, Function<E, String> keyFn, Function<E, String> pkeyFn) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<String, List<E>> childrenMap = new HashMap<>();
        for (E e : list) {
            String pid = pkeyFn.apply(e);
            if (pid != null) {
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(e);
            }
        }
        List<E> result = new ArrayList<>();
        collectChildren(childrenMap, keyFn, id, result);
        return result;
    }

    private static <E> void collectChildren(Map<String, List<E>> childrenMap, Function<E, String> keyFn, String parentId, List<E> result) {
        List<E> children = childrenMap.get(parentId);
        if (children != null) {
            for (E child : children) {
                result.add(child);
                collectChildren(childrenMap, keyFn, keyFn.apply(child), result);
            }
        }
    }

    /**
     * 从 map 中查找父节点
     */
    public static <E> E getParent(Map<String, E> map, E node, Function<E, String> pkeyFn) {
        if (node == null) {
            return null;
        }
        String pid = pkeyFn.apply(node);
        return pid == null ? null : map.get(pid);
    }

    /**
     * 向上查找匹配 predicate 的祖先节点
     */
    public static <E> E getParent(Map<String, E> map, E node, Function<E, String> pkeyFn, Predicate<E> predicate) {
        E parent = getParent(map, node, pkeyFn);
        while (parent != null) {
            if (predicate.test(parent)) {
                return parent;
            }
            parent = getParent(map, parent, pkeyFn);
        }
        return null;
    }

    /**
     * 从树构建层级 Map（根节点 level = 1）
     */
    public static <E> Map<String, Integer> buildLevelMap(List<E> tree, Function<E, String> keyFn, Function<E, List<E>> getChildren) {
        Map<String, Integer> levelMap = new HashMap<>();
        if (CollUtil.isEmpty(tree)) {
            return levelMap;
        }
        for (E root : tree) {
            String id = keyFn.apply(root);
            levelMap.put(id, 1);
            setChildLevel(root, keyFn, getChildren, levelMap);
        }
        return levelMap;
    }

    private static <E> void setChildLevel(E parent, Function<E, String> keyFn, Function<E, List<E>> getChildren, Map<String, Integer> levelMap) {
        List<E> children = getChildren.apply(parent);
        if (children != null) {
            for (E child : children) {
                String cid = keyFn.apply(child);
                levelMap.put(cid, levelMap.get(keyFn.apply(parent)) + 1);
                setChildLevel(child, keyFn, getChildren, levelMap);
            }
        }
    }

    public static List<TreeOption> buildTree(List<TreeOption> list) {
        return buildTree(list, TreeOption::getKey, TreeOption::getParentKey, TreeOption::getChildren, TreeOption::setChildren);
    }

    public static Map<String, TreeOption> treeToMap(List<TreeOption> tree) {
        Map<String, TreeOption> map = new HashMap<>();
        walk(tree, TreeOption::getChildren, node -> map.put(node.getKey(), node));
        return map;
    }

    public static <E> Map<String, E> treeToMap(List<E> tree, Function<E, String> keyFn, Function<E, List<E>> getChildren) {
        Map<String, E> map = new HashMap<>();
        walk(tree, getChildren, node -> {
            String key = keyFn.apply(node);
            map.put(key, node);
        });
        return map;
    }

    /**
     * 构造树
     *
     * @param list
     * @param keyFn
     * @param pkeyFn
     * @param <E>
     * @return
     */
    public static <E> List<E> buildTree(List<E> list, Function<E, String> keyFn, Function<E, String> pkeyFn, Function<E, List<E>> getChildren, BiConsumer<E, List<E>> setChildren) {
        Map<String, E> keyMap = new HashMap<>();
        for (E e : list) {
            keyMap.put(keyFn.apply(e), e);
        }

        List<E> tree = new ArrayList<>();

        for (E e : list) {
            String pid = pkeyFn.apply(e);
            E parent = keyMap.get(pid);

            if (parent == null) {
                tree.add(e);
                continue;
            }

            List<E> parentChildren = getChildren.apply(parent); // 父节点的children字段
            if (parentChildren == null) {
                parentChildren = new ArrayList<>();
                setChildren.accept(parent, parentChildren);
            }
            parentChildren.add(e);
        }

        cleanEmptyChildren(tree, getChildren, setChildren);
        return tree;
    }


    public static <E> void cleanEmptyChildren(List<E> list, Function<E, List<E>> getChildren, BiConsumer<E, List<E>> setChildrenFn) {
        walk(list, getChildren, e -> {
            List<E> children = getChildren.apply(e);
            if (CollUtil.isEmpty(children)) {
                setChildrenFn.accept(e, null);
            }
        });
    }


    /**
     * 递归树并处理子树下的节点
     *
     * @param consumer 节点处理器
     */
    public static <E> void walk(List<E> list, Function<E, List<E>> getChildren, Consumer<E> consumer) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (E e : list) {
            consumer.accept(e);
            List<E> children = getChildren.apply(e);
            walk(children, getChildren, consumer);
        }
    }


    /**
     * 递归树并处理子树下的节点
     *
     * @param consumer 节点处理器, 两个参数，分别是节点，节点的父节点
     */
    public static <E> void walk(List<E> list, Function<E, List<E>> getChildren, BiConsumer<E, E> consumer) {
        walk(null, list, getChildren, consumer);
    }

    private static <E> void walk(E parent, List<E> list, Function<E, List<E>> getChildren, BiConsumer<E, E> consumer) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (E e : list) {
            consumer.accept(e, parent);
            List<E> children = getChildren.apply(e);
            walk(e, children, getChildren, consumer);
        }
    }

    /**
     * 获取一棵树的叶子
     *
     * @param <E>
     * @return
     */
    public static <E> List<E> getLeafs(List<E> list, Function<E, List<E>> getChildren) {
        List<E> result = new ArrayList<>();
        walk(list, getChildren, e -> {
            List<E> children = getChildren.apply(e);
            boolean isLeaf = CollUtil.isEmpty(children);
            if (isLeaf) {
                result.add(e);
            }
        });

        return result;
    }


    public static <E> List<E> treeToList(List<E> tree, Function<E, List<E>> getChildren) {
        List<E> list = new ArrayList<>();
        walk(tree, getChildren, e -> list.add(e));
        return list;
    }


    /**
     * 获取节点的父节点列表
     *
     * @param <E>
     * @param list 注意不是树，而是列表
     * @return 从根节点到父节点的顺序
     */
    public static <E> List<String> getPids(String nodeId, List<E> list, Function<E, String> keyFn, Function<E, String> pkeyFn) {
        Map<String, E> idMap = new HashMap<>();
        for (E e : list) {
            idMap.put(keyFn.apply(e), e);
        }
        E node = idMap.get(nodeId);
        if (node == null) {
            return Collections.emptyList();
        }

        List<String> pids = new ArrayList<>();

        String pid = pkeyFn.apply(node);
        E parent = idMap.get(pid);
        while (parent != null) {
            pids.add(pid);
            pid = pkeyFn.apply(parent);
            parent = idMap.get(pid);
        }

        // 反转列表，使根节点在前
        Collections.reverse(pids);
        return pids;

    }

    public static <E> void removeIf(List<E> list, Function<E, List<E>> getChildren, Predicate<? super E> filter) {
        if (list == null) {
            return;
        }
        list.removeIf(filter);
        walk(list, getChildren, t -> {
            List<E> children = getChildren.apply(t);
            removeIf(children, getChildren, filter);
        });
    }
}
