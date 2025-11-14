package io.admin.common.utils.tree.drop;

import io.admin.common.antd.DropEvent;
import io.admin.common.antd.TreeNodeItem;
import io.admin.common.utils.tree.TreeTool;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 树拖拽排序
public class TreeDropTool {


    /**
     * 计算拖拽排序
     */

    public static DropResult onDrop(DropEvent e, List<TreeNodeItem> tree) {
        Map<String, TreeNodeItem> keyMap = TreeTool.treeToMap(tree);


        TreeNodeItem dragNode = keyMap.get(e.getDragKey());
        TreeNodeItem dropNode = keyMap.get(e.getDropKey());
        Assert.notNull(dragNode, "拖拽的节点不存在");
        Assert.notNull(dropNode, "放置的节点不存在");

        DropResult result = new DropResult();
        result.parentKey = e.isDropToGap() ? dropNode.getParentKey() : dropNode.getKey();

        TreeNodeItem parentNode = keyMap.get(result.getParentKey());
        List<TreeNodeItem> siblings = parentNode != null ? parentNode.getChildren() : tree; // 如果父节点为空，说明拖拽到了根节点平级了


        List<String> keys = new ArrayList<>();
        for (TreeNodeItem child : siblings) {
            keys.add(child.getKey());
        }
        result.sortedKeys = resort(keys, e);
        return result;
    }


    public static List<String> resort(List<String> list, DropEvent e) {
        String k = e.getDragKey();
        if (e.getDropPosition() == DropEvent.DropPosition.INSIDE) {
            return list;
        }

        list.remove(k);
        int index = list.indexOf(e.getDropKey());
        if (e.getDropPosition() == DropEvent.DropPosition.BOTTOM) {
            index++;
        }
        list.add(index, k);

        return list;
    }


}
