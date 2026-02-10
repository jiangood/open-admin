package io.github.jiangood.openadmin.lang.dto.antd;

// https://ant.design/components/menu-cn#itemtype

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItem implements Cloneable {
    private Boolean danger; //	展示错误状态样式	boolean	false
    private Boolean disabled; //	是否禁用	boolean	false
    private String extra; //	额外节点	ReactNode	-	5.21.0
    private AntdIcon icon; //	菜单图标	ReactNode	-
    private String key; //item 的唯一标志	string	-
    private String label; //	菜单项标题	ReactNode	-
    private String title; //    设置收缩时展示的悬浮标题 string


    private List<MenuItem> children;


    // 额外字段，用于组装树
    @JsonIgnore
    private String parentKey;


    private String path;

    private String type;

    @Override
    public MenuItem clone() {
        try {
            MenuItem clone = (MenuItem) super.clone();
            // 深拷贝可变字段
            if (this.children != null) {
                clone.children = this.children.stream()
                        .map(MenuItem::clone)
                        .collect(Collectors.toList());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
