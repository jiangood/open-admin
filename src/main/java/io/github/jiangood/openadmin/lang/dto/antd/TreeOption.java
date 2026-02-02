package io.github.jiangood.openadmin.lang.dto.antd;

// https://ant.design/components/tree-cn#treenode-props

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(of = "key")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class TreeOption {

    private String key;//	被树的 (default)ExpandedKeys / (default)CheckedKeys / (default)SelectedKeys 属性所用。注意：整个树范围内的所有节点的 key 值不能重复！	string	(内部计算出的节点位置)
    private String title;//	标题	ReactNode	---
    private Boolean checkable; //	当树为 checkable 时，设置独立节点是否展示 Checkbox	boolean	-
    private Boolean disableCheckbox;//	禁掉 checkbox	boolean	false
    private Boolean disabled;//	禁掉响应	boolean	false
    private String icon;//	自定义图标。可接收组件，props 为当前节点 props	ReactNode | (props) => ReactNode	-
    @JsonProperty("isLeaf")
    private Boolean isLeaf;//	设置为叶子节点 (设置了 loadData 时有效)。为 false 时会强制将其作为父节点	boolean	-
    private Boolean selectable;//	设置节点是否可被选中	boolean	true
    private List<TreeOption> children;
    // DirectoryTree props
    private String expandAction; //	目录展开逻辑，可选：false | click | doubleClick	string | boolean	click
    // 额外字段，方便构建树
    private String parentKey;
    // 直接设置icon前端无法显示的，设置成这个，前端再转换为ReactNode
    private String iconName;

    public TreeOption(String title, String key, String parentKey) {
        this.key = key;
        this.title = title;
        this.parentKey = parentKey;
    }

    // 兼容选择框
    //https://ant.design/components/tree-select-cn#treenode-props
    public String getValue() {
        return key;
    }
}
