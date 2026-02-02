package io.github.jiangood.openadmin.lang.tree;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TreeManager工具类的单元测试
 */
class TreeManagerTest {

    // 测试用的TreeNode实现类
    static class TestTreeNode implements TreeNode<TestTreeNode> {
        private String id;
        private String pid;
        private List<TestTreeNode> children;
        private boolean isLeaf;

        public TestTreeNode(String id, String pid) {
            this.id = id;
            this.pid = pid;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getPid() {
            return pid;
        }

        @Override
        public void setPid(String pid) {
            this.pid = pid;
        }

        @Override
        public List<TestTreeNode> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<TestTreeNode> children) {
            this.children = children;
        }

        @Override
        public void setIsLeaf(Boolean isLeaf) {
            this.isLeaf = isLeaf;
        }

        public boolean isLeaf() {
            return isLeaf;
        }
    }

    @Test
    void testBuildTree() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));
        nodes.add(new TestTreeNode("5", "2"));
        nodes.add(new TestTreeNode("6", "3"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 验证树的构建
        List<TestTreeNode> tree = treeManager.getTree();
        assertNotNull(tree);
        assertEquals(1, tree.size());
        TestTreeNode root = tree.get(0);
        assertEquals("1", root.getId());
        assertNotNull(root.getChildren());
        assertEquals(2, root.getChildren().size());

        // 验证map的构建
        Map<String, TestTreeNode> map = treeManager.getMap();
        assertNotNull(map);
        assertEquals(6, map.size());
        assertNotNull(map.get("1"));
        assertNotNull(map.get("2"));
        assertNotNull(map.get("3"));
        assertNotNull(map.get("4"));
        assertNotNull(map.get("5"));
        assertNotNull(map.get("6"));
    }

    @Test
    void testTraverseTree() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试遍历树
        List<String> traversalResult = new ArrayList<>();
        treeManager.traverseTree(treeManager.getTree(), node -> traversalResult.add(node.getId()));

        assertNotNull(traversalResult);
        assertEquals(3, traversalResult.size());
        assertEquals("1", traversalResult.get(0));
        assertEquals("2", traversalResult.get(1));
        assertEquals("3", traversalResult.get(2));
    }

    @Test
    void testTraverseTreeFromLeaf() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试从叶子节点遍历
        List<String> traversalResult = new ArrayList<>();
        treeManager.traverseTreeFromLeaf(node -> traversalResult.add(node.getId()));

        assertNotNull(traversalResult);
        assertEquals(4, traversalResult.size());
        // 验证遍历顺序：叶子节点先遍历
        assertTrue(traversalResult.contains("4"));
        assertTrue(traversalResult.contains("2"));
        assertTrue(traversalResult.contains("3"));
        assertTrue(traversalResult.contains("1"));
    }

    @Test
    void testGetSortedList() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取排序后的列表
        List<TestTreeNode> sortedList = treeManager.getSortedList();

        assertNotNull(sortedList);
        assertEquals(3, sortedList.size());
        assertEquals("1", sortedList.get(0).getId());
        assertEquals("2", sortedList.get(1).getId());
        assertEquals("3", sortedList.get(2).getId());
    }

    @Test
    void testGetParentById() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取父节点
        TestTreeNode parentOf3 = treeManager.getParentById("3");
        assertNotNull(parentOf3);
        assertEquals("2", parentOf3.getId());

        TestTreeNode parentOf2 = treeManager.getParentById("2");
        assertNotNull(parentOf2);
        assertEquals("1", parentOf2.getId());

        TestTreeNode parentOf1 = treeManager.getParentById("1");
        assertNull(parentOf1);

        TestTreeNode parentOfNonExistent = treeManager.getParentById("999");
        assertNull(parentOfNonExistent);
    }

    @Test
    void testGetAllChildren() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));
        nodes.add(new TestTreeNode("5", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取所有子节点
        List<TestTreeNode> childrenOf1 = treeManager.getAllChildren("1");
        assertNotNull(childrenOf1);
        assertEquals(4, childrenOf1.size());

        List<TestTreeNode> childrenOf2 = treeManager.getAllChildren("2");
        assertNotNull(childrenOf2);
        assertEquals(2, childrenOf2.size());

        List<TestTreeNode> childrenOf4 = treeManager.getAllChildren("4");
        assertNotNull(childrenOf4);
        assertEquals(0, childrenOf4.size());

        List<TestTreeNode> childrenOfNonExistent = treeManager.getAllChildren("999");
        assertNotNull(childrenOfNonExistent);
        assertEquals(0, childrenOfNonExistent.size());
    }

    @Test
    void testIsLeaf() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试是否为叶子节点
        assertFalse(treeManager.isLeaf("1"));
        assertFalse(treeManager.isLeaf("2"));
        assertTrue(treeManager.isLeaf("3"));

        // 测试通过节点对象判断
        TestTreeNode node1 = treeManager.getMap().get("1");
        TestTreeNode node2 = treeManager.getMap().get("2");
        TestTreeNode node3 = treeManager.getMap().get("3");

        assertFalse(treeManager.isLeaf(node1));
        assertFalse(treeManager.isLeaf(node2));
        assertTrue(treeManager.isLeaf(node3));
    }

    @Test
    void testGetLeafCount() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));
        nodes.add(new TestTreeNode("5", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取叶子节点数量
        TestTreeNode node1 = treeManager.getMap().get("1");
        TestTreeNode node2 = treeManager.getMap().get("2");
        TestTreeNode node4 = treeManager.getMap().get("4");

        assertEquals(3, treeManager.getLeafCount(node1));
        assertEquals(2, treeManager.getLeafCount(node2));
        assertEquals(1, treeManager.getLeafCount(node4));
        assertEquals(0, treeManager.getLeafCount(null));
    }

    @Test
    void testGetLeafList() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取叶子节点列表
        List<TestTreeNode> leafList = treeManager.getLeafList();
        assertNotNull(leafList);
        assertEquals(2, leafList.size());
        assertTrue(leafList.stream().anyMatch(node -> "3".equals(node.getId())));
        assertTrue(leafList.stream().anyMatch(node -> "4".equals(node.getId())));
    }

    @Test
    void testGetLeafIdList() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取叶子节点ID列表
        List<String> leafIdList = treeManager.getLeafIdList();
        assertNotNull(leafIdList);
        assertEquals(2, leafIdList.size());
        assertTrue(leafIdList.contains("3"));
        assertTrue(leafIdList.contains("4"));
    }

    @Test
    void testGetParentIdListById() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取父节点ID列表
        List<String> parentIdsOf3 = treeManager.getParentIdListById("3");
        assertNotNull(parentIdsOf3);
        assertEquals(2, parentIdsOf3.size());
        assertEquals("1", parentIdsOf3.get(0));
        assertEquals("2", parentIdsOf3.get(1));

        List<String> parentIdsOf2 = treeManager.getParentIdListById("2");
        assertNotNull(parentIdsOf2);
        assertEquals(1, parentIdsOf2.size());
        assertEquals("1", parentIdsOf2.get(0));

        List<String> parentIdsOf1 = treeManager.getParentIdListById("1");
        assertNotNull(parentIdsOf1);
        assertEquals(0, parentIdsOf1.size());

        List<String> parentIdsOfNonExistent = treeManager.getParentIdListById("999");
        assertNotNull(parentIdsOfNonExistent);
        assertEquals(0, parentIdsOfNonExistent.size());
    }

    @Test
    void testGetIdsByLevel() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "1"));
        nodes.add(new TestTreeNode("4", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试通过层级获取ID列表
        List<String> level1Ids = treeManager.getIdsByLevel(1);
        assertNotNull(level1Ids);
        assertEquals(1, level1Ids.size());
        assertEquals("1", level1Ids.get(0));

        List<String> level2Ids = treeManager.getIdsByLevel(2);
        assertNotNull(level2Ids);
        assertEquals(2, level2Ids.size());
        assertTrue(level2Ids.contains("2"));
        assertTrue(level2Ids.contains("3"));

        List<String> level3Ids = treeManager.getIdsByLevel(3);
        assertNotNull(level3Ids);
        assertEquals(1, level3Ids.size());
        assertEquals("4", level3Ids.get(0));

        List<String> level4Ids = treeManager.getIdsByLevel(4);
        assertNotNull(level4Ids);
        assertEquals(0, level4Ids.size());
    }

    @Test
    void testGetLevelById() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试获取节点层级
        assertEquals(1, treeManager.getLevelById("1"));
        assertEquals(2, treeManager.getLevelById("2"));
        assertEquals(3, treeManager.getLevelById("3"));
        assertNull(treeManager.getLevelById("999"));
    }

    @Test
    void testBuildLevelMap() {
        // 构建测试数据
        List<TestTreeNode> nodes = new ArrayList<>();
        nodes.add(new TestTreeNode("1", null));
        nodes.add(new TestTreeNode("2", "1"));
        nodes.add(new TestTreeNode("3", "2"));

        // 创建TreeManager
        TreeManager<TestTreeNode> treeManager = TreeManager.of(nodes);

        // 测试构建层级Map
        Map<String, Integer> levelMap = treeManager.buildLevelMap();
        assertNotNull(levelMap);
        assertEquals(3, levelMap.size());
        assertEquals(1, levelMap.get("1"));
        assertEquals(2, levelMap.get("2"));
        assertEquals(3, levelMap.get("3"));
    }
}
