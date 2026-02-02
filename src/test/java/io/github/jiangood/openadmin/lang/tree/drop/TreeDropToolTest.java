package io.github.jiangood.openadmin.lang.tree.drop;

import io.github.jiangood.openadmin.lang.dto.antd.DropEvent;
import io.github.jiangood.openadmin.lang.dto.antd.TreeOption;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreeDropToolTest {

    @Test
    void testOnDropWithInsidePosition() {
        // 准备测试数据
        List<TreeOption> tree = createTestTree();
        DropEvent event = new DropEvent();
        event.setDragKey("3"); // 拖拽节点3
        event.setDropKey("2"); // 放置到节点2
        event.setDropPosition(0); // 放置到内部 (0)
        event.setDropToGap(false);

        // 执行测试
        DropResult result = TreeDropTool.onDrop(event, tree);

        // 验证结果
        assertNotNull(result);
        assertEquals("2", result.parentKey); // 父节点应该是2
        assertNotNull(result.sortedKeys);
        assertEquals(1, result.sortedKeys.size());
        assertEquals("3", result.sortedKeys.get(0)); // 应该添加到子节点列表开头
    }

    @Test
    void testOnDropWithTopPosition() {
        // 准备测试数据
        List<TreeOption> tree = createTestTree();
        DropEvent event = new DropEvent();
        event.setDragKey("3"); // 拖拽节点3
        event.setDropKey("2"); // 放置到节点2
        event.setDropPosition(-1); // 放置到上方 (-1)
        event.setDropToGap(true);

        // 执行测试
        DropResult result = TreeDropTool.onDrop(event, tree);

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.parentKey); // 父节点应该是1
        assertNotNull(result.sortedKeys);
        assertEquals(2, result.sortedKeys.size());
        assertEquals("3", result.sortedKeys.get(0)); // 应该在节点2前面
        assertEquals("2", result.sortedKeys.get(1));
    }

    @Test
    void testOnDropWithBottomPosition() {
        // 准备测试数据
        List<TreeOption> tree = createTestTree();
        DropEvent event = new DropEvent();
        event.setDragKey("3"); // 拖拽节点3
        event.setDropKey("2"); // 放置到节点2
        event.setDropPosition(1); // 放置到下方 (1)
        event.setDropToGap(true);

        // 执行测试
        DropResult result = TreeDropTool.onDrop(event, tree);

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.parentKey); // 父节点应该是1
        assertNotNull(result.sortedKeys);
        assertEquals(2, result.sortedKeys.size());
        assertEquals("2", result.sortedKeys.get(0));
        assertEquals("3", result.sortedKeys.get(1)); // 应该在节点2后面
    }

    @Test
    void testOnDropWithRootLevel() {
        // 准备测试数据
        List<TreeOption> tree = createTestTree();
        DropEvent event = new DropEvent();
        event.setDragKey("3"); // 拖拽节点3
        event.setDropKey("1"); // 放置到根节点1
        event.setDropPosition(1); // 放置到下方 (1)
        event.setDropToGap(true);

        // 执行测试
        DropResult result = TreeDropTool.onDrop(event, tree);

        // 验证结果
        assertNotNull(result);
        assertNull(result.parentKey); // 应该是根节点级别
        assertNotNull(result.sortedKeys);
        assertEquals(2, result.sortedKeys.size());
        assertEquals("1", result.sortedKeys.get(0));
        assertEquals("3", result.sortedKeys.get(1)); // 应该在根节点1后面
    }

    @Test
    void testOnDropWithNonExistentDragNode() {
        // 准备测试数据
        List<TreeOption> tree = createTestTree();
        DropEvent event = new DropEvent();
        event.setDragKey("999"); // 不存在的节点
        event.setDropKey("2");
        event.setDropPosition(0); // 放置到内部
        event.setDropToGap(false);

        // 执行测试 - 应该抛出异常
        assertThrows(Exception.class, () -> {
            TreeDropTool.onDrop(event, tree);
        });
    }

    @Test
    void testOnDropWithNonExistentDropNode() {
        // 准备测试数据
        List<TreeOption> tree = createTestTree();
        DropEvent event = new DropEvent();
        event.setDragKey("3");
        event.setDropKey("999"); // 不存在的节点
        event.setDropPosition(0); // 放置到内部
        event.setDropToGap(false);

        // 执行测试 - 应该抛出异常
        assertThrows(Exception.class, () -> {
            TreeDropTool.onDrop(event, tree);
        });
    }

    @Test
    void testResortWithInsidePosition() {
        // 准备测试数据
        List<String> list = new ArrayList<>();
        list.add("2");
        DropEvent event = new DropEvent();
        event.setDragKey("3");
        event.setDropKey("2");
        event.setDropPosition(0); // 放置到内部
        event.setDropPositionEnum(DropEvent.DropPositionEnum.INSIDE);

        // 执行测试
        List<String> result = TreeDropTool.resort(list, event);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("3", result.get(0)); // 应该添加到开头
        assertEquals("2", result.get(1));
    }

    @Test
    void testResortWithTopPosition() {
        // 准备测试数据
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("4");
        DropEvent event = new DropEvent();
        event.setDragKey("3");
        event.setDropKey("2");
        event.setDropPosition(-1); // 放置到上方
        event.setDropPositionEnum(DropEvent.DropPositionEnum.TOP);

        // 执行测试
        List<String> result = TreeDropTool.resort(list, event);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("1", result.get(0));
        assertEquals("3", result.get(1)); // 应该在2前面
        assertEquals("2", result.get(2));
        assertEquals("4", result.get(3));
    }

    @Test
    void testResortWithBottomPosition() {
        // 准备测试数据
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("4");
        DropEvent event = new DropEvent();
        event.setDragKey("3");
        event.setDropKey("2");
        event.setDropPosition(1); // 放置到下方
        event.setDropPositionEnum(DropEvent.DropPositionEnum.BOTTOM);

        // 执行测试
        List<String> result = TreeDropTool.resort(list, event);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("1", result.get(0));
        assertEquals("2", result.get(1));
        assertEquals("3", result.get(2)); // 应该在2后面
        assertEquals("4", result.get(3));
    }

    @Test
    void testResortWithExistingKey() {
        // 准备测试数据
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        DropEvent event = new DropEvent();
        event.setDragKey("3"); // 已经存在的key
        event.setDropKey("1");
        event.setDropPosition(1); // 放置到下方
        event.setDropPositionEnum(DropEvent.DropPositionEnum.BOTTOM);

        // 执行测试
        List<String> result = TreeDropTool.resort(list, event);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("1", result.get(0));
        assertEquals("3", result.get(1)); // 应该移动到1后面
        assertEquals("2", result.get(2));
    }

    @Test
    void testResortWithEmptyList() {
        // 准备测试数据
        List<String> list = new ArrayList<>();
        DropEvent event = new DropEvent();
        event.setDragKey("1");
        event.setDropKey("2");
        event.setDropPosition(0); // 放置到内部
        event.setDropPositionEnum(DropEvent.DropPositionEnum.INSIDE);

        // 执行测试
        List<String> result = TreeDropTool.resort(list, event);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0));
    }

    // 创建测试用的树结构
    private List<TreeOption> createTestTree() {
        List<TreeOption> tree = new ArrayList<>();

        // 创建根节点1
        TreeOption node1 = new TreeOption();
        node1.setKey("1");
        node1.setTitle("Node 1");

        // 创建节点2作为节点1的子节点
        TreeOption node2 = new TreeOption();
        node2.setKey("2");
        node2.setTitle("Node 2");
        node2.setParentKey("1");

        // 创建节点3作为节点2的子节点
        TreeOption node3 = new TreeOption();
        node3.setKey("3");
        node3.setTitle("Node 3");
        node3.setParentKey("2");

        // 构建树结构
        List<TreeOption> children1 = new ArrayList<>();
        children1.add(node2);
        node1.setChildren(children1);

        List<TreeOption> children2 = new ArrayList<>();
        children2.add(node3);
        node2.setChildren(children2);

        tree.add(node1);
        return tree;
    }

}
