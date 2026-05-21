package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.config.SysMenuDef;
import io.github.jiangood.openadmin.modules.system.dto.MenuItem;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class SysMenuService {

    private final SysMenuRepository sysMenuRepository;

    public List<SysMenuDef> findAll() {
        return sysMenuRepository.findAll();
    }

    public List<SysMenuDef> menuTree() {
        List<SysMenuDef> all = sysMenuRepository.findAll();
        return TreeTool.buildTree(all, SysMenuDef::getId, SysMenuDef::getPid, SysMenuDef::getChildren, SysMenuDef::setChildren);
    }

    public Dict buildMenuInfo(List<SysMenuDef> menuDefs) {
        Map<String, SysMenuDef> pathMenuMap = new HashMap<>();
        Map<String, SysMenuDef> menuMap = new HashMap<>();
        List<MenuItem> list = menuDefs.stream()
                .filter(def -> def.getDisabled() == null || !def.getDisabled())
                .map(def -> {
                    MenuItem item = new MenuItem();
                    item.setKey(def.getId());
                    Assert.notNull(def.getName(), "菜单名称不能为空");
                    item.setLabel(def.getName());
                    item.setTitle(def.getName().substring(0, 1));
                    item.setParentKey(def.getPid());
                    item.setIcon(def.getIcon());
                    item.setPath(StrUtil.nullToEmpty(def.getPath()));

                    if (def.getPath() != null) {
                        pathMenuMap.put(def.getPath(), def);
                    }
                    menuMap.put(def.getId(), def);

                    return item;
                }).toList();

        List<MenuItem> tree = TreeTool.buildTree(list, MenuItem::getKey, MenuItem::getParentKey, MenuItem::getChildren, MenuItem::setChildren);
        Dict data = new Dict();
        data.put("menuTree", tree);
        data.put("menuMap", menuMap);
        data.put("pathMenuMap", pathMenuMap);
        return data;
    }

}
