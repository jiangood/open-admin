package io.github.jiangood.openadmin.modules.system.dao.impl;

import io.github.jiangood.openadmin.framework.config.datadefinition.DataPropertiesFactory;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SysMenuRepositoryYamlImpl implements SysMenuRepository {

    @Override
    public List<MenuDefinition> findAll() {
       return DataPropertiesFactory.getInstance().getMenus();
    }

    @Override
    public List<MenuDefinition> findAllEnabled() {
        List<MenuDefinition> list = this.findAll();
        TreeTool.removeIf(list, MenuDefinition::getChildren, m->m.getDisabled() != null && m.getDisabled());
        return list;
    }


    @Override
    public List<MenuDefinition> findAllById(List<String> ids) {
        List<MenuDefinition> list = this.findAll();
        return list.stream().filter(t -> ids.contains(t.getId())).toList();
    }



}
