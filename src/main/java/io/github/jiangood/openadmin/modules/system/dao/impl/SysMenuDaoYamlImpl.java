package io.github.jiangood.openadmin.modules.system.dao.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.jiangood.openadmin.framework.config.datadefinition.DataPropertiesFactory;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.datadefinition.DataProperties;
import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.lang.ResourceTool;
import io.github.jiangood.openadmin.lang.YmlTool;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.modules.system.dao.SysMenuDao;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class SysMenuDaoYamlImpl implements SysMenuDao {

    @Override
    public List<MenuDefinition> findAll() {
       return DataPropertiesFactory.getInstance().getMenus();
    }

    @Override
    public List<MenuDefinition> findAllEnabled() {
        List<MenuDefinition> list = this.findAll();
        TreeTool.removeIf(list, MenuDefinition::getChildren, MenuDefinition::isDisabled);
        return list;
    }


    @Override
    public List<MenuDefinition> findAllById(List<String> ids) {
        List<MenuDefinition> list = this.findAll();
        return list.stream().filter(t -> ids.contains(t.getId())).toList();
    }



}
