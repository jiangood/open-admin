package io.github.jiangood.openadmin.modules.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItem {
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

    public MenuItem() {
    }

    public MenuItem(MenuItem src) {
        this.danger = src.danger;
        this.disabled = src.disabled;
        this.extra = src.extra;
        this.icon = src.icon;
        this.key = src.key;
        this.label = src.label;
        this.parentKey = src.parentKey;
        this.path = src.path;
        this.type = src.type;
        if (src.children != null) {
            this.children = src.children.stream().map(MenuItem::new).toList();
        }
    }
}
