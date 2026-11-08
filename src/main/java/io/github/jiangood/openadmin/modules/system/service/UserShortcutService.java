package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterShortcutVO;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人中心快捷菜单。
 * <p>
 * 快捷菜单只保存菜单 id，读取时再按当前角色权限解析名称/路径/图标。
 * 角色权限被回收、菜单被删除或被业务定制隐藏后，快捷项会自动失效，
 * 不会把无权限入口展示给用户。
 */
@Service
@RequiredArgsConstructor
public class UserShortcutService {

    public static final int MAX_SHORTCUTS = 10;

    private final SysUserRepository sysUserRepository;
    private final UserMenuService userMenuService;

    /**
     * 当前用户有效的快捷菜单，按保存顺序返回。
     */
    @Transactional(readOnly = true)
    public List<UserCenterShortcutVO> list(String userId) {
        SysUser user = requireUser(userId);
        Map<String, MenuDefinition> pageMap = pageMenuMap(user);
        return normalize(user.getShortcutMenus()).stream()
                .map(pageMap::get)
                .filter(Objects::nonNull)
                .map(this::toVO)
                .toList();
    }

    /**
     * 快捷菜单配置项：当前用户有权限的页面菜单树。
     * <p>
     * 目录节点仅用于分组，前端设置 {@code disableCheckbox}，只能勾选叶子页面菜单。
     */
    @Transactional(readOnly = true)
    public List<TreeOption> options(String userId) {
        SysUser user = requireUser(userId);
        List<MenuDefinition> menus = userMenuService.resolve(user);
        Map<String, MenuDefinition> menuMap = menus.stream()
                .filter(menu -> !menu.isDivider())
                .collect(Collectors.toMap(MenuDefinition::getId, menu -> menu,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Set<String> pageIds = pageMenus(menus).stream()
                .map(MenuDefinition::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (pageIds.isEmpty()) {
            return List.of();
        }

        // 页面菜单 + 其祖先目录，保证树形结构完整
        Set<String> includeIds = new LinkedHashSet<>();
        for (String pageId : pageIds) {
            String current = pageId;
            while (current != null && includeIds.add(current)) {
                MenuDefinition menu = menuMap.get(current);
                current = menu == null ? null : menu.getPid();
            }
        }

        List<TreeOption> nodes = menus.stream()
                .filter(menu -> !menu.isDivider())
                .filter(menu -> includeIds.contains(menu.getId()))
                .map(menu -> {
                    TreeOption node = new TreeOption(menu.getName(), menu.getId(), menu.getPid());
                    boolean page = pageIds.contains(menu.getId());
                    node.setIcon(menu.getIcon());
                    node.setLeaf(page);
                    node.setDisableCheckbox(!page);
                    node.setSelectable(page);
                    return node;
                })
                .toList();
        return TreeTool.buildTree(nodes);
    }

    /**
     * 保存快捷菜单，返回保存后的有效列表。
     */
    @Transactional
    public List<UserCenterShortcutVO> save(String userId, List<String> menuIds) {
        SysUser user = requireUser(userId);
        List<String> ids = normalize(menuIds);
        Assert.isTrue(ids.size() <= MAX_SHORTCUTS, "快捷菜单最多选择 " + MAX_SHORTCUTS + " 个");

        Map<String, MenuDefinition> pageMap = pageMenuMap(user);
        boolean invalid = ids.stream().anyMatch(id -> !pageMap.containsKey(id));
        Assert.isTrue(!invalid, "快捷菜单包含无权访问或不可跳转的菜单");

        user.setShortcutMenus(new ArrayList<>(ids));
        sysUserRepository.save(user);
        return ids.stream().map(pageMap::get).map(this::toVO).toList();
    }

    private SysUser requireUser(String userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        Assert.notNull(user, "用户不存在");
        return user;
    }

    private Map<String, MenuDefinition> pageMenuMap(SysUser user) {
        return pageMenus(userMenuService.resolve(user)).stream()
                .collect(Collectors.toMap(MenuDefinition::getId, menu -> menu,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private List<MenuDefinition> pageMenus(List<MenuDefinition> menus) {
        return menus.stream()
                .filter(menu -> !menu.isDivider())
                .filter(menu -> menu.getPath() != null && !menu.getPath().isBlank())
                .filter(menu -> menu.getName() != null && !menu.getName().isBlank())
                .toList();
    }

    private List<String> normalize(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .toList();
    }

    private UserCenterShortcutVO toVO(MenuDefinition menu) {
        UserCenterShortcutVO vo = new UserCenterShortcutVO();
        vo.setId(menu.getId());
        vo.setName(menu.getName());
        vo.setPath(menu.getPath());
        vo.setIcon(menu.getIcon());
        return vo;
    }

}
