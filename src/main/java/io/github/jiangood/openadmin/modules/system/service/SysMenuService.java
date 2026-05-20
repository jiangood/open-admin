package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统菜单service接口实现类
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysMenuService {


    private final SysMenuRepository sysMenuRepository;


    public List<MenuDefinition> findAll() {
        return sysMenuRepository.findAll();
    }


    public List<MenuDefinition> menuTree() {
        List<MenuDefinition> all = sysMenuRepository.findAll();
        List<MenuDefinition> tree = TreeTool.buildTree(all, MenuDefinition::getId, MenuDefinition::getPid, MenuDefinition::getChildren, MenuDefinition::setChildren);
        return tree;

    }


}
