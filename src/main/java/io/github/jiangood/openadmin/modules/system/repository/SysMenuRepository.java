package io.github.jiangood.openadmin.modules.system.repository;


import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;

import java.util.List;


public interface SysMenuRepository {


    List<MenuDefinition> findAll();

    List<MenuDefinition> findAllEnabled();

    List<MenuDefinition> findAllById(List<String> ids);
}
