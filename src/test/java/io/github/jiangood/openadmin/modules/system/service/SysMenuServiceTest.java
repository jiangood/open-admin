package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.dto.MenuPermTreeNode;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysMenuServiceTest {

    @Mock
    private SysMenuRepository sysMenuRepository;

    private SysMenuService sysMenuService;

    @BeforeEach
    void setUp() {
        sysMenuService = new SysMenuService(sysMenuRepository);
    }

    private MenuDefinition menu(String id, String pid, String name, MenuDefinition.PermDefinition... perms) {
        MenuDefinition def = new MenuDefinition();
        def.setId(id);
        def.setPid(pid);
        def.setName(name);
        if (perms.length > 0) {
            def.setPerms(Arrays.asList(perms));
        }
        return def;
    }

    private MenuDefinition.PermDefinition perm(String name, String code) {
        MenuDefinition.PermDefinition p = new MenuDefinition.PermDefinition();
        p.setName(name);
        p.setCode(code);
        return p;
    }

    @Test
    void testMenuPermTree_shouldBuildNestedTreeWithPermCodesAndNames() {
        MenuDefinition sys = menu("sys", null, "系统管理");
        MenuDefinition user = menu("sys-user", "sys", "用户管理",
                perm("读取", "read"), perm("创建", "create"));
        MenuDefinition role = menu("sys-role", "sys", "角色管理",
                perm("读取", "read"));

        when(sysMenuRepository.findAll()).thenReturn(Arrays.asList(sys, user, role));

        List<MenuPermTreeNode> tree = sysMenuService.menuPermTree();

        assertEquals(1, tree.size());
        MenuPermTreeNode root = tree.get(0);
        assertEquals("sys", root.getId());
        assertEquals("系统管理", root.getName());
        assertNull(root.getPid());
        assertTrue(root.getPermCodes().isEmpty());
        assertTrue(root.getPermNames().isEmpty());
        assertNotNull(root.getChildren());
        assertEquals(2, root.getChildren().size());

        MenuPermTreeNode userNode = root.getChildren().get(0);
        assertEquals("sys-user", userNode.getId());
        assertEquals("用户管理", userNode.getName());
        assertEquals(List.of("sys-user:read", "sys-user:create"), userNode.getPermCodes());
        assertEquals(List.of("读取", "创建"), userNode.getPermNames());

        MenuPermTreeNode roleNode = root.getChildren().get(1);
        assertEquals("sys-role", roleNode.getId());
        assertEquals(List.of("sys-role:read"), roleNode.getPermCodes());
        assertEquals(List.of("读取"), roleNode.getPermNames());
        assertNull(roleNode.getChildren());
    }

    @Test
    void testMenuPermTree_whenNoMenus_shouldReturnEmptyList() {
        when(sysMenuRepository.findAll()).thenReturn(List.of());

        List<MenuPermTreeNode> tree = sysMenuService.menuPermTree();

        assertNotNull(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    void testMenuPermTree_shouldPropagateDisabledFlag() {
        MenuDefinition sys = menu("sys", null, "系统管理");
        sys.setDisabled(true);

        when(sysMenuRepository.findAll()).thenReturn(List.of(sys));

        List<MenuPermTreeNode> tree = sysMenuService.menuPermTree();

        assertEquals(Boolean.TRUE, tree.get(0).getDisabled());
    }
}
