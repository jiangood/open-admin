package io.github.jiangood.as.modules.system.service;

import io.github.jiangood.as.common.tools.tree.TreeTool;
import io.github.jiangood.as.framework.config.data.dto.MenuDefinition;
import io.github.jiangood.as.modules.system.dao.SysMenuDao;
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
