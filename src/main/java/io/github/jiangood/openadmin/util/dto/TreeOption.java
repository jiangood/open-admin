package io.github.jiangood.openadmin.util.dto;

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

    private String key;
    /** 选择值，恒等于 key（antd TreeSelect 等组件依赖 value 字段） */
    private String value;
    private String title;
    private Boolean checkable;
    private Boolean disableCheckbox;
    private Boolean disabled;
    private String icon;
    @JsonProperty("isLeaf")
    private Boolean leaf;
    private Boolean selectable;
    private List<TreeOption> children;
    private String expandAction;
    private String parentKey;
    private String iconName;

    public TreeOption(String title, String key, String parentKey) {
        this.key = key;
        this.value = key;
        this.title = title;
        this.parentKey = parentKey;
    }

    public void setKey(String key) {
        this.key = key;
        this.value = key;
    }

    public void setValue(String value) {
        this.value = value;
        this.key = value;
    }
}
