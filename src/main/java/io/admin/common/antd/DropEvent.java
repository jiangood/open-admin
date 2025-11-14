package io.admin.common.antd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * dropPosition	-1	放置在目标节点的 前面 (作为兄弟节点)。
 * dropPosition	0	放置在目标节点的 内部 (作为子节点)。
 * dropPosition	1	放置在目标节点的 后面 (作为兄弟节点)。
 *
 * dropToGap	true	放置在两个兄弟节点之间的 空隙 (Gap) 中。
 * dropToGap	false	放置在目标节点 内部 (作为子节点)。
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropEvent {

    String dragKey;
    String dropKey;

    boolean dropToGap; // 两个节点的关系，true表示平级

    //  the drop position relative to the drop node, inside 0, top -1, bottom 1
    // 前端转换后的相对位置
    DropPosition dropPosition;


    public enum DropPosition {
        INSIDE,
        TOP,
        BOTTOM
    }
}
