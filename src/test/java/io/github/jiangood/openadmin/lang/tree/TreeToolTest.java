package io.github.jiangood.openadmin.lang.tree;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.lang.dto.antd.TreeOption;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TreeTool工具类的单元测试
 */
class TreeToolTest {

    // 测试用的树节点类
    static class TestNode {
        private String id;
        private String parentId;
        private List<TestNode> children;

        public TestNode(String id, String parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return parentId;
        }

        public List<TestNode> getChildren() {
            return children;
        }

        public void setChildren(List<TestNode> children) {
            this.children = children;
        }
    }

    @Test
    void testBuildTree() {
        // 构建测试数据
        List<TreeOption> options = new ArrayList<>();
        TreeOption option1 = new TreeOption();
        option1.setKey("1");
        option1.setParentKey(null);
        options.add(option1);

        TreeOption option2 = new TreeOption();
        option2.setKey("2");
        option2.setParentKey("1");
        options.add(option2);

        TreeOption option3 = new TreeOption();
        option3.setKey("3");
        option3.setParentKey("1");
        options.add(option3);

        // 测试构建树
        List<TreeOption> tree = TreeTool.buildTree(options);
        assertNotNull(tree);
        assertEquals(1, tree.size());
        TreeOption root = tree.get(0);
        assertEquals("1", root.getKey());
        assertNotNull(root.getChildren());
        assertEquals(2, root.getChildren().size());
    }

    @Test
    void testTreeToMap() {
        // 构建测试数据
        List<TreeOption> options = new ArrayList<>();
        TreeOption option1 = new TreeOption();
        option1.setKey("1");
        option1.setParentKey(null);
        options.add(option1);

        TreeOption option2 = new TreeOption();
        option2.setKey("2");
        option2.setParentKey("1");
        options.add(option2);

        // 构建树
        List<TreeOption> tree = TreeTool.buildTree(options);

        // 测试树转Map
        Map<String, TreeOption> map = TreeTool.treeToMap(tree);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertNotNull(map.get("1"));
        assertNotNull(map.get("2"));
    }

    @Test
    void testTreeToMapWithCustomFunctions() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 测试树转Map
        Map<String, TestNode> map = TreeTool.treeToMap(tree, TestNode::getId, TestNode::getChildren);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertNotNull(map.get("1"));
        assertNotNull(map.get("2"));
    }

    @Test
    void testBuildTreeByDict() {
        // 构建测试数据
        List<Dict> dicts = new ArrayList<>();
        Dict dict1 = Dict.create().set("key", "1").set("parentKey", null);
        dicts.add(dict1);

        Dict dict2 = Dict.create().set("key", "2").set("parentKey", "1");
        dicts.add(dict2);

        // 测试通过Dict构建树
        List<Dict> tree = TreeTool.buildTreeByDict(dicts);
        assertNotNull(tree);
        assertEquals(1, tree.size());
        Dict root = tree.get(0);
        assertEquals("1", root.get("key"));
        List<Dict> children = (List<Dict>) root.get("children");
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("2", children.get(0).get("key"));
    }

    @Test
    void testCleanEmptyChildren() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 确保子节点列表存在
        assertNotNull(tree.get(0).getChildren());

        // 测试清理空的子节点列表
        TreeTool.cleanEmptyChildren(tree, TestNode::getChildren, TestNode::setChildren);

        // 验证清理结果
        assertNotNull(tree.get(0).getChildren());
        assertNull(tree.get(0).getChildren().get(0).getChildren());
    }

    @Test
    void testWalk() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        TestNode node3 = new TestNode("3", "2");
        nodes.add(node3);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 测试遍历树
        List<String> traversalResult = new ArrayList<>();
        TreeTool.walk(tree, TestNode::getChildren, node -> traversalResult.add(node.getId()));

        assertNotNull(traversalResult);
        assertEquals(3, traversalResult.size());
        assertEquals("1", traversalResult.get(0));
        assertEquals("2", traversalResult.get(1));
        assertEquals("3", traversalResult.get(2));
    }

    @Test
    void testWalkWithParent() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 测试带父节点的遍历
        List<String> traversalResult = new ArrayList<>();
        TreeTool.walk(tree, TestNode::getChildren, (node, parent) -> {
            if (parent != null) {
                traversalResult.add(node.getId() + "_child_of_" + parent.getId());
            } else {
                traversalResult.add(node.getId() + "_root");
            }
        });

        assertNotNull(traversalResult);
        assertEquals(2, traversalResult.size());
        assertEquals("1_root", traversalResult.get(0));
        assertEquals("2_child_of_1", traversalResult.get(1));
    }

    @Test
    void testGetLeafs() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        TestNode node3 = new TestNode("3", "1");
        nodes.add(node3);

        TestNode node4 = new TestNode("4", "2");
        nodes.add(node4);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 测试获取叶子节点
        List<TestNode> leafs = TreeTool.getLeafs(tree, TestNode::getChildren);
        assertNotNull(leafs);
        assertEquals(2, leafs.size());
        assertTrue(leafs.stream().anyMatch(node -> "3".equals(node.getId())));
        assertTrue(leafs.stream().anyMatch(node -> "4".equals(node.getId())));
    }

    @Test
    void testTreeToList() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 测试树转列表
        List<TestNode> list = TreeTool.treeToList(tree, TestNode::getChildren);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(node -> "1".equals(node.getId())));
        assertTrue(list.stream().anyMatch(node -> "2".equals(node.getId())));
    }

    @Test
    void testGetPids() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        nodes.add(new TestNode("1", null));
        nodes.add(new TestNode("2", "1"));
        nodes.add(new TestNode("3", "2"));

        // 测试获取父节点ID列表
        List<String> pids = TreeTool.getPids("3", nodes, TestNode::getId, TestNode::getParentId);
        assertNotNull(pids);
        assertEquals(2, pids.size());
        assertEquals("1", pids.get(0));
        assertEquals("2", pids.get(1));

        List<String> pidsOf2 = TreeTool.getPids("2", nodes, TestNode::getId, TestNode::getParentId);
        assertNotNull(pidsOf2);
        assertEquals(1, pidsOf2.size());
        assertEquals("1", pidsOf2.get(0));

        List<String> pidsOf1 = TreeTool.getPids("1", nodes, TestNode::getId, TestNode::getParentId);
        assertNotNull(pidsOf1);
        assertEquals(0, pidsOf1.size());

        List<String> pidsOfNonExistent = TreeTool.getPids("999", nodes, TestNode::getId, TestNode::getParentId);
        assertNotNull(pidsOfNonExistent);
        assertEquals(0, pidsOfNonExistent.size());
    }

    @Test
    void testRemoveIf() {
        // 构建测试数据
        List<TestNode> nodes = new ArrayList<>();
        TestNode node1 = new TestNode("1", null);
        nodes.add(node1);

        TestNode node2 = new TestNode("2", "1");
        nodes.add(node2);

        TestNode node3 = new TestNode("3", "1");
        nodes.add(node3);

        // 构建树
        List<TestNode> tree = TreeTool.buildTree(nodes, TestNode::getId, TestNode::getParentId, TestNode::getChildren, TestNode::setChildren);

        // 测试移除节点
        TreeTool.removeIf(tree, TestNode::getChildren, node -> "2".equals(node.getId()));

        assertNotNull(tree);
        assertEquals(1, tree.size());
        TestNode root = tree.get(0);
        assertNotNull(root.getChildren());
        assertEquals(1, root.getChildren().size());
        assertEquals("3", root.getChildren().get(0).getId());
    }
}
