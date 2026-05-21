package io.github.jiangood.openadmin.modules.system.repository;


import io.github.jiangood.openadmin.framework.config.SysMenuDef;

import java.util.List;


public interface SysMenuRepository {


    List<SysMenuDef> findAll();

    List<SysMenuDef> findAllById(List<String> ids);
}
