package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.dto.MenuItem;
import io.github.jiangood.openadmin.modules.system.dto.MenuPermTreeNode;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.util.dto.TreeOption;
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

    public List<MenuDefinition> findAll() {
        return sysMenuRepository.findAll();
    }

    public List<TreeOption> menuTree() {
        List<MenuDefinition> all = sysMenuRepository.findAll();
        List<TreeOption> items = all.stream().map(def -> {
            TreeOption node = new TreeOption(def.getName(), def.getId(), def.getPid());
            node.setDisabled(def.getDisabled());
            return node;
        }).toList();
        return TreeTool.buildTree(items);
    }

    public List<MenuPermTreeNode> menuPermTree() {
        List<MenuDefinition> all = sysMenuRepository.findAll();
        List<MenuPermTreeNode> nodes = all.stream().map(def -> {
            MenuPermTreeNode node = new MenuPermTreeNode();
            node.setId(def.getId());
            node.setPid(def.getPid());
            node.setName(def.getName());
            node.setPermCodes(def.getPermCodes());
            node.setPermNames(def.getPermNames());
            node.setDisabled(def.getDisabled());
            return node;
        }).toList();
        return TreeTool.buildTree(nodes, MenuPermTreeNode::getId, MenuPermTreeNode::getPid,
                MenuPermTreeNode::getChildren, MenuPermTreeNode::setChildren);
    }

    public Dict buildMenuInfo(List<MenuDefinition> menuDefs) {
        Map<String, MenuDefinition> pathMenuMap = new HashMap<>();
        Map<String, MenuDefinition> menuMap = new HashMap<>();
        List<MenuItem> list = menuDefs.stream()
                .filter(def -> def.getDisabled() == null || !def.getDisabled())
                .map(def -> {
                    MenuItem item = new MenuItem();
                    item.setKey(def.getId());
                    Assert.notNull(def.getName(), "菜单名称不能为空");
                    item.setLabel(def.getName());

                    item.setParentKey(def.getPid());
                    item.setIcon(def.getIcon());
                    item.setPath(CharSequenceUtil.nullToEmpty(def.getPath()));

                    if (def.getPath() != null) {
                        pathMenuMap.put(def.getPath(), def);
                    }
                    menuMap.put(def.getId(), def);

                    return item;
                }).toList();

        List<MenuItem> tree = TreeTool.buildTree(list, MenuItem::getKey, MenuItem::getParentKey, MenuItem::getChildren, MenuItem::setChildren);

        TreeTool.walk(tree, MenuItem::getChildren, item -> {
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                item.setType("directory");
            } else {
                item.setType("menu");
            }
        });

        Dict data = new Dict();
        data.put("menuTree", tree);
        data.put("menuMap", menuMap);
        data.put("pathMenuMap", pathMenuMap);
        return data;
    }

}
