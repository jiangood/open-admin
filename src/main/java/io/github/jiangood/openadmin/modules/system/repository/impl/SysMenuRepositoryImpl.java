package io.github.jiangood.openadmin.modules.system.repository.impl;

import io.github.jiangood.openadmin.framework.config.MenuYamlLoader;
import io.github.jiangood.openadmin.framework.config.SysMenuDef;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SysMenuRepositoryImpl implements SysMenuRepository {

    private final List<SysMenuDef> menus;

    public SysMenuRepositoryImpl() {
        this.menus = MenuYamlLoader.load();
    }

    @Override
    public List<SysMenuDef> findAll() {
        return menus;
    }

    @Override
    public List<SysMenuDef> findAllById(List<String> ids) {
        return menus.stream().filter(t -> ids.contains(t.getId())).toList();
    }

}
