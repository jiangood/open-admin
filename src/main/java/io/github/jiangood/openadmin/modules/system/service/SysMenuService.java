package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.dao.SysMenuDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统菜单service接口实现类
 */
@Service
@Slf4j
public class SysMenuService {


    @Resource
    private SysMenuDao sysMenuDao;


    public List<MenuDefinition> findAll() {
        return sysMenuDao.findAll();
    }


    public List<MenuDefinition> menuTree() {
        List<MenuDefinition> all = sysMenuDao.findAll();
        List<MenuDefinition> tree = TreeTool.buildTree(all, MenuDefinition::getId, MenuDefinition::getPid, MenuDefinition::getChildren, MenuDefinition::setChildren);
        return tree;

    }


}
