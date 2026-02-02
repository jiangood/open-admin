package io.github.jiangood.openadmin.lang.tree.drop;

import io.github.jiangood.openadmin.lang.dto.antd.DropEvent;
import io.github.jiangood.openadmin.lang.dto.antd.TreeOption;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 树拖拽排序
public class TreeDropTool {


    /**
     * 计算拖拽排序
     */

    public static DropResult onDrop(DropEvent e, List<TreeOption> tree) {
        e.setDropPositionEnum(DropEvent.DropPositionEnum.valueOf(e.getDropPosition()));

        Map<String, TreeOption> keyMap = TreeTool.treeToMap(tree);

        TreeOption dragNode = keyMap.get(e.getDragKey());
        TreeOption dropNode = keyMap.get(e.getDropKey());
        Assert.notNull(dragNode, "拖拽的节点不存在");
        Assert.notNull(dropNode, "放置的节点不存在");

        DropResult result = new DropResult();
        result.parentKey = e.isDropToGap() ? dropNode.getParentKey() : dropNode.getKey();
        TreeOption parentNode = keyMap.get(result.getParentKey());
        List<TreeOption> siblings = parentNode != null ? parentNode.getChildren() : tree; // 如果父节点为空，说明拖拽到了根节点平级了


        List<String> keys = new ArrayList<>();
        if (siblings != null) {
            for (TreeOption child : siblings) {
                keys.add(child.getKey());
            }
        }

        result.sortedKeys = resort(keys, e);
        return result;
    }


    public static List<String> resort(List<String> list, DropEvent e) {
        String k = e.getDragKey();
        if (e.getDropPositionEnum() == DropEvent.DropPositionEnum.INSIDE) {
            list.remove(k); // 先移除，避免重复添加
            list.add(0, k);
            return list;
        }

        list.remove(k);
        int index = list.indexOf(e.getDropKey());
        if (e.getDropPositionEnum() == DropEvent.DropPositionEnum.BOTTOM) {
            index++;
        }
        list.add(index, k);

        return list;
    }


}
