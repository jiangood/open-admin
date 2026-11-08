package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.framework.spi.MenuCustomization;
import io.github.jiangood.openadmin.framework.spi.MenuCustomizer;
import io.github.jiangood.openadmin.framework.spi.MenuOverride;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 当前用户最终菜单解析。
 * <p>
 * 先按角色过滤出被授权的菜单，再交给业务项目提供的
 * {@link MenuCustomizer} 做展示层调整（改名、移动、排序、隐藏）。
 * 角色授权页和用户权限页不经过本类，仍展示原始菜单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final SysRoleService sysRoleService;
    private final Optional<MenuCustomizer> menuCustomizer;

    public List<MenuDefinition> resolve(SysUser user) {
        List<MenuDefinition> originals = sysRoleService.ownMenu(user.getRoles());
        if (menuCustomizer.isEmpty()) {
            return originals;
        }

        Map<String, MenuDefinition> working = copyMenus(originals);
        MenuCustomization customization = new MenuCustomization(working.values());
        try {
            menuCustomizer.get().customize(customization);
        } catch (Exception e) {
            log.error("菜单定制失败，已忽略全部定制：{}",
                    menuCustomizer.get().getClass().getName(), e);
            return originals;
        }

        return apply(customization, working);
    }

    private static Map<String, MenuDefinition> copyMenus(List<MenuDefinition> menus) {
        Map<String, MenuDefinition> map = new LinkedHashMap<>();
        for (MenuDefinition menu : menus) {
            map.put(menu.getId(), copy(menu));
        }
        return map;
    }

    private static MenuDefinition copy(MenuDefinition src) {
        MenuDefinition target = new MenuDefinition();
        target.setId(src.getId());
        target.setPid(src.getPid());
        target.setName(src.getName());
        target.setIcon(src.getIcon());
        target.setPath(src.getPath());
        target.setSeq(src.getSeq());
        target.setMessageCountUrl(src.getMessageCountUrl());
        target.setType(src.getType());

        List<MenuDefinition.PermDefinition> perms = new ArrayList<>();
        if (src.getPerms() != null) {
            for (MenuDefinition.PermDefinition perm : src.getPerms()) {
                MenuDefinition.PermDefinition copy = new MenuDefinition.PermDefinition();
                copy.setName(perm.getName());
                copy.setCode(perm.getCode());
                perms.add(copy);
            }
        }
        target.setPerms(perms);
        return target;
    }

    private List<MenuDefinition> apply(MenuCustomization customization,
                                       Map<String, MenuDefinition> working) {
        Set<String> warned = new HashSet<>();

        for (MenuOverride override : customization.getOverrides()) {
            MenuDefinition menu = working.get(override.id());
            if (menu == null) {
                warnOnce(warned, "菜单定制：菜单 {} 不存在，已忽略", override.id());
                continue;
            }
            if (override.name() != null) {
                if (override.name().isBlank()) {
                    warnOnce(warned, "菜单定制：菜单 {} 的名称不能为空，已忽略", override.id());
                } else {
                    menu.setName(override.name().trim());
                }
            }
            if (override.seq() != null) {
                menu.setSeq(override.seq());
            }
            if (override.pid() != null) {
                String pid = override.pid().isBlank() ? null : override.pid();
                if (isPidValid(menu, pid, working)) {
                    menu.setPid(pid);
                } else {
                    warnOnce(warned, "菜单定制：菜单 {} 的新父节点 {} 无效，已忽略",
                            override.id(), override.pid());
                }
            }
        }

        List<MenuDefinition> result = new ArrayList<>(working.values());
        result.sort(Comparator.comparingInt(MenuDefinition::getSeq));

        Set<String> hidden = expandHidden(customization.getHidden(), working, warned);
        if (!hidden.isEmpty()) {
            result.removeIf(menu -> hidden.contains(menu.getId()));
        }

        applyMoves(result, customization.getMoves(), warned);

        int seq = 10;
        for (MenuDefinition menu : result) {
            menu.setSeq(seq);
            seq += 10;
        }
        return result;
    }

    private boolean isPidValid(MenuDefinition menu, String pid, Map<String, MenuDefinition> working) {
        if (pid == null) {
            return true;
        }
        if (pid.equals(menu.getId())) {
            return false;
        }
        MenuDefinition parent = working.get(pid);
        if (parent == null) {
            return false;
        }
        if (parent.isDivider()) {
            return false;
        }
        if (parent.getPath() != null && !parent.getPath().isBlank()) {
            return false;
        }

        Set<String> seen = new HashSet<>();
        String current = pid;
        while (current != null) {
            if (!seen.add(current) || current.equals(menu.getId())) {
                return false;
            }
            MenuDefinition node = working.get(current);
            current = node == null ? null : node.getPid();
        }
        return true;
    }

    private Set<String> expandHidden(Set<String> hiddenIds,
                                     Map<String, MenuDefinition> working,
                                     Set<String> warned) {
        if (hiddenIds.isEmpty()) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        for (String id : hiddenIds) {
            if (!working.containsKey(id)) {
                warnOnce(warned, "菜单定制：菜单 {} 不存在，无法隐藏", id);
                continue;
            }
            stack.push(id);
        }
        while (!stack.isEmpty()) {
            String id = stack.pop();
            if (!result.add(id)) {
                continue;
            }
            for (MenuDefinition menu : working.values()) {
                if (id.equals(menu.getPid())) {
                    stack.push(menu.getId());
                }
            }
        }
        return result;
    }

    private void applyMoves(List<MenuDefinition> menus,
                            List<MenuCustomization.RelativeMove> moves,
                            Set<String> warned) {
        for (MenuCustomization.RelativeMove move : moves) {
            MenuDefinition node = findById(menus, move.id());
            MenuDefinition target = findById(menus, move.targetId());
            if (node == null) {
                warnOnce(warned, "菜单定制：菜单 {} 不存在，无法移动", move.id());
                continue;
            }
            if (target == null) {
                warnOnce(warned, "菜单定制：目标菜单 {} 不存在，无法移动 {}",
                        move.targetId(), move.id());
                continue;
            }
            if (Objects.equals(node.getId(), target.getId())) {
                warnOnce(warned, "菜单定制：菜单 {} 不能相对自己移动", move.id());
                continue;
            }
            if (!Objects.equals(node.getPid(), target.getPid())) {
                warnOnce(warned, "菜单定制：菜单 {} 与目标 {} 不在同一父节点下，无法相对移动",
                        move.id(), move.targetId());
                continue;
            }

            menus.removeIf(menu -> menu.getId().equals(move.id()));
            int index = indexOfId(menus, move.targetId());
            if (index < 0) {
                continue;
            }
            menus.add(move.before() ? index : index + 1, node);
        }
    }

    private static MenuDefinition findById(List<MenuDefinition> menus, String id) {
        for (MenuDefinition menu : menus) {
            if (menu.getId().equals(id)) {
                return menu;
            }
        }
        return null;
    }

    private static int indexOfId(List<MenuDefinition> menus, String id) {
        for (int i = 0; i < menus.size(); i++) {
            if (menus.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static void warnOnce(Set<String> warned, String message, Object... args) {
        String key = message + Arrays.toString(args);
        if (warned.add(key)) {
            log.warn(message, args);
        }
    }
}
