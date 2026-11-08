package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterShortcutVO;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserShortcutServiceTest {

    @Mock
    private SysUserRepository sysUserRepository;

    @Mock
    private UserMenuService userMenuService;

    private UserShortcutService userShortcutService;

    private SysUser user;

    @BeforeEach
    void setUp() {
        userShortcutService = new UserShortcutService(sysUserRepository, userMenuService);

        user = new SysUser();
        user.setId("u1");
        user.setAccount("admin");
        SysRole role = new SysRole();
        role.setId("r1");
        role.setCode("admin");
        role.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role)));

        when(sysUserRepository.findById("u1")).thenReturn(Optional.of(user));
    }

    private MenuDefinition menu(String id, String pid, String name, String path) {
        MenuDefinition menu = new MenuDefinition();
        menu.setId(id);
        menu.setPid(pid);
        menu.setName(name);
        menu.setPath(path);
        return menu;
    }

    private List<MenuDefinition> menus() {
        MenuDefinition root = menu("sys", null, "系统管理", null);
        MenuDefinition userMenu = menu("sys-user", "sys", "用户管理", "/system/user");
        MenuDefinition roleMenu = menu("sys-role", "sys", "角色管理", "/system/role");
        MenuDefinition divider = new MenuDefinition();
        divider.setId("sys-sep");
        divider.setPid("sys");
        divider.setType(MenuDefinition.TYPE_DIVIDER);
        MenuDefinition pathless = menu("sys-dir", "sys", "空目录", null);
        return List.of(root, userMenu, roleMenu, divider, pathless);
    }

    @Test
    void list_shouldKeepSavedOrderAndSkipInvalidMenus() {
        user.setShortcutMenus(List.of("sys-role", "missing", "sys-user", "sys-role"));
        when(userMenuService.resolve(user)).thenReturn(menus());

        List<UserCenterShortcutVO> list = userShortcutService.list("u1");

        assertEquals(List.of("sys-role", "sys-user"), list.stream().map(UserCenterShortcutVO::getId).toList());
        assertEquals("/system/role", list.get(0).getPath());
    }

    @Test
    void options_shouldContainOnlyAuthorizedPageMenusWithAncestors() {
        when(userMenuService.resolve(user)).thenReturn(menus());

        List<TreeOption> tree = userShortcutService.options("u1");

        assertEquals(1, tree.size());
        TreeOption root = tree.get(0);
        assertEquals("sys", root.getKey());
        assertTrue(root.getDisableCheckbox());
        assertFalse(root.getSelectable());
        assertEquals(List.of("sys-user", "sys-role"),
                root.getChildren().stream().map(TreeOption::getKey).toList());
        assertTrue(root.getChildren().stream().allMatch(node -> Boolean.TRUE.equals(node.getLeaf())));
        assertTrue(root.getChildren().stream().noneMatch(node -> Boolean.TRUE.equals(node.getDisableCheckbox())));
    }

    @Test
    void options_shouldUseResolvedMenuName() {
        List<MenuDefinition> resolved = menus();
        resolved.get(1).setName("自定义用户管理");
        when(userMenuService.resolve(user)).thenReturn(resolved);

        List<TreeOption> tree = userShortcutService.options("u1");

        assertEquals("自定义用户管理", tree.get(0).getChildren().get(0).getTitle());
    }

    @Test
    void save_shouldRejectMoreThanLimit() {
        List<String> ids = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(i -> "menu-" + i)
                .toList();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userShortcutService.save("u1", ids));

        assertEquals("快捷菜单最多选择 10 个", ex.getMessage());
    }

    @Test
    void save_shouldRejectUnauthorizedMenu() {
        when(userMenuService.resolve(user)).thenReturn(menus());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userShortcutService.save("u1", List.of("sys-user", "not-exist")));

        assertEquals("快捷菜单包含无权访问或不可跳转的菜单", ex.getMessage());
    }

    @Test
    void save_shouldPersistNormalizedOrder() {
        when(userMenuService.resolve(user)).thenReturn(menus());

        List<UserCenterShortcutVO> saved = userShortcutService.save("u1",
                List.of("sys-role", " ", "sys-user", "sys-role"));

        assertEquals(List.of("sys-role", "sys-user"), saved.stream().map(UserCenterShortcutVO::getId).toList());
        assertEquals(List.of("sys-role", "sys-user"), user.getShortcutMenus());
        verify(sysUserRepository).save(user);
    }

    @Test
    void save_shouldAllowClearing() {
        user.setShortcutMenus(List.of("sys-user"));
        when(userMenuService.resolve(user)).thenReturn(menus());

        List<UserCenterShortcutVO> saved = userShortcutService.save("u1", List.of());

        assertTrue(saved.isEmpty());
        assertTrue(user.getShortcutMenus().isEmpty());
        verify(sysUserRepository).save(user);
    }
}
