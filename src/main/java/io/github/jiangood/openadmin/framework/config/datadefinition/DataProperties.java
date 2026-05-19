package io.github.jiangood.openadmin.framework.config.datadefinition;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataProperties {
    /**
     * 菜单定义和
     */
    private List<MenuDefinition> menus = new ArrayList<>();


    /**
     * 字典定义
     */
    private List<DictDefinition> dicts = new ArrayList<>();

}
