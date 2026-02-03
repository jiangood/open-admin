package io.github.jiangood.openadmin.lang.tree;

import lombok.Getter;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 树形管理， 内部包含很多数据，及方法
 */
public class TreeManager<T extends TreeNode<T>> {
    private static final int ROOT_LEVEL = 1;

    private final List<T> list;

    @Getter
    private List<T> tree;

    @Getter
    private Map<String, T> map;


    private TreeManager(List<T> list) {
        this.list = list;
        buildMap();
        buildTree();
        cleanLeafChildren();

        // 设置是否叶子
        for (T x : list) {
            x.setIsLeaf(this.isLeaf(x));
        }
    }


    public static <X extends TreeNode<X>> TreeManager<X> of(List<X> dataList) {
        TreeManager<X> tm = new TreeManager<>(dataList);

        return tm;
    }

    public static <X> void traverseTree(Collection<X> treeList, Function<X, List<X>> getChildrenFn, TraverseAction<X> traverseAction) {
        for (X item : treeList) {
            traverseAction.performAction(item);
            List<X> children = getChildrenFn.apply(item);
            if (children != null && children.size() > 0) {
                traverseTree(children, getChildrenFn, traverseAction);
            }
        }
    }

    public void traverseTree(List<T> treeList, TraverseAction<T> traverseAction) {
        for (T item : treeList) {
            traverseAction.performAction(item);
            List<T> children = item.getChildren();
            if (children != null && !children.isEmpty()) {
                traverseTree(children, traverseAction);
            }
        }
    }

    /**
     * 遍历树，从最深子节点到，父节点，
     *
     * @param traverseAction 回调
     */
    public void traverseTreeFromLeaf(TraverseAction<T> traverseAction) {
        Map<String, Integer> levelMap = this.buildLevelMap();
        int maxLevel = levelMap.values().stream().mapToInt(t -> t).max().getAsInt();

        for (int i = maxLevel; i >= ROOT_LEVEL; i--) {

            for (Map.Entry<String, Integer> e : levelMap.entrySet()) {
                String id = e.getKey();
                Integer level = e.getValue();
                if (level == i) {
                    traverseAction.performAction(getMap().get(id));
                }
            }
        }
    }

    public List<T> getSortedList() {
        List<T> list = new ArrayList<>();
        List<T> treeList = this.getTree();

        this.traverseTree(treeList, list::add);

        return list;
    }

    public T getParentById(String id) {
        T t = map.get(id);
        if (t == null) {
            return null;
        }
        String pid = t.getPid();
        return map.get(pid);
    }

    private void buildMap() {
        map = new HashMap<>();
        for (T t : list) {
            String id = t.getId();
            map.put(id, t);
        }
    }

    private void buildTree() {
        tree = new ArrayList<>(list.size() / 2);

        for (T t : list) {
            String pid = t.getPid();
            T parent = map.get(pid);
            if (parent != null) {
                List<T> children = initChildren(parent);
                children.add(t);
            } else {
                // root node
                tree.add(t);
            }
        }
    }

    private List<T> initChildren(T node) {
        List<T> children = node.getChildren();
        if (children != null) {
            return children;
        }

        children = new ArrayList<>();
        node.setChildren(children);
        return children;
    }

    private void cleanLeafChildren() {
        for (T t : getMap().values()) {
            if (isLeaf(t)) {
                t.setChildren(null);
            }
        }
    }

    public List<T> getAllChildren(String id) {
        T node = map.get(id);
        if (node == null) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();

        addChildToResult(node, result);

        return result;
    }

    public T getParent(T t) {
        String pid = t.getPid();
        return map.get(pid);
    }

    public T getParent(T t, Function<T, Boolean> util) {
        T parent = this.getParent(t);
        if (parent == null) {
            return null;
        }
        Boolean result = util.apply(parent);
        if (result) {
            return parent;
        } else {
            return getParent(parent, util);
        }
    }

    public boolean isLeaf(String id) {
        T t = map.get(id);
        return isLeaf(t);
    }

    public boolean isLeaf(T t) {
        if (t == null) {
            return false;
        }
        List<T> children = t.getChildren();
        return children == null || children.isEmpty();
    }

    public int getLeafCount(T t) {
        if (t == null) {
            return 0;
        }

        AtomicInteger count = new AtomicInteger(0);
        traverseTree(Collections.singletonList(t), item -> {
            boolean leaf = isLeaf(item);
            if (leaf) {
                count.incrementAndGet();
            }
        });

        return count.get();
    }

    public List<T> getLeafList() {
        List<T> result = new ArrayList<>();
        for (T t : map.values()) {
            if (isLeaf(t)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<String> getLeafIdList() {
        List<String> result = new ArrayList<>();
        for (T t : map.values()) {
            if (isLeaf(t)) {
                result.add(t.getId());
            }
        }
        return result;
    }

    public List<String> getParentIdListById(String id) {
        List<String> ids = new ArrayList<>();

        T t = map.get(id);
        if (t == null) {
            return Collections.emptyList();
        }
        T parent = getParent(t);

        while (parent != null) {
            String parentId = parent.getId();
            ids.add(0, parentId);

            T temp = getParent(parent);
            parent = temp;
        }
        return ids;
    }

    /**
     * 通过层级查询
     *
     * @param level 开始于 TreeManager.ROOT_LEVEL
     * @return 结果id列表
     */
    public List<String> getIdsByLevel(int level) {
        Map<String, Integer> lm = buildLevelMap();

        List<String> result = new ArrayList<>();

        for (Map.Entry<String, Integer> e : lm.entrySet()) {
            if (e.getValue() == level) {
                result.add(e.getKey());
            }
        }

        return result;
    }

    public int getLevelById(String id) {
        Map<String, Integer> lm = buildLevelMap();
        Integer level = lm.get(id);
        Assert.state(level != null, "id not found:" + id);
        return level;
    }

    public Map<String, Integer> buildLevelMap() {
        Map<String, Integer> result = new HashMap<>();

        for (T t : tree) {
            String id = t.getId();
            result.put(id, ROOT_LEVEL);
            setChildLevel(t, result);
        }

        return result;
    }

    private void setChildLevel(T t, Map<String, Integer> levelMap) {
        String id = t.getId();
        List<T> children = t.getChildren();
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                String cid = child.getId();
                levelMap.put(cid, levelMap.get(id) + 1);
                setChildLevel(child, levelMap);
            }
        }
    }

    private void addChildToResult(T t, List<T> result) {
        List<T> children = t.getChildren();
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                result.add(child);
                addChildToResult(child, result);
            }
        }
    }


    public interface TraverseAction<T> {
        void performAction(T item);
    }


}
