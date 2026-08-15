package io.github.jiangood.openadmin.modules.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItem implements Cloneable {
    private Boolean danger;
    private Boolean disabled;
    private String extra;
    /** Ant Design 图标组件名，取值见 https://ant-design.antgroup.com/components/icon-cn */
    private String icon;
    private String key;
    private String label;

    private List<MenuItem> children;

    @JsonIgnore
    private String parentKey;

    private String path;

    private String type;

    @Override
    public MenuItem clone() {
        try {
            MenuItem clone = (MenuItem) super.clone();
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
