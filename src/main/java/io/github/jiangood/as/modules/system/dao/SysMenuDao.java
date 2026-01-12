package io.github.jiangood.as.modules.system.dao;


import io.github.jiangood.as.framework.config.data.dto.MenuDefinition;

import java.util.List;


public interface SysMenuDao {


    List<MenuDefinition> findAll();

    List<MenuDefinition> findAllEnabled();

    List<MenuDefinition> findAllById(List<String> ids);
}
