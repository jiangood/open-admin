package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.SysMenuDef;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {

    @Mock
    private SysRoleRepository roleRepository;

    @Mock
    private SysMenuRepository sysMenuRepository;

    private SysRoleService sysRoleService;

    @BeforeEach
    void setUp() {
        sysRoleService = new SysRoleService(roleRepository, sysMenuRepository);
    }

    @Test
    void testOwnMenu_whenRoleIsAdmin_shouldReturnAllMenus() {
        SysRole adminRole = new SysRole();
        adminRole.setId("1");
        adminRole.setCode("admin");
        adminRole.setBuiltin(true);

        SysMenuDef menu1 = new SysMenuDef();
        menu1.setId("sys");
        menu1.setName("系统管理");
        menu1.setSeq(1);

        SysMenuDef menu2 = new SysMenuDef();
        menu2.setId("sys-user");
        menu2.setName("用户管理");
        menu2.setPid("sys");
        menu2.setSeq(2);

        List<SysMenuDef> allMenus = Arrays.asList(menu1, menu2);

        when(roleRepository.findById("1")).thenReturn(Optional.of(adminRole));
        when(sysMenuRepository.findAll()).thenReturn(allMenus);

        List<SysMenuDef> result = sysRoleService.ownMenu("1");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sysMenuRepository, times(1)).findAll();
        verify(sysMenuRepository, never()).findAllById(any());
    }

    @Test
    void testOwnMenu_whenRoleIsNotAdmin_shouldReturnRoleMenus() {
        SysRole normalRole = new SysRole();
        normalRole.setId("2");
        normalRole.setCode("normal");
        normalRole.setBuiltin(false);
        normalRole.setMenus(Arrays.asList("sys", "sys-user"));

        SysMenuDef menu1 = new SysMenuDef();
        menu1.setId("sys");
        menu1.setName("系统管理");
        menu1.setSeq(1);

        SysMenuDef menu2 = new SysMenuDef();
        menu2.setId("sys-user");
        menu2.setName("用户管理");
        menu2.setPid("sys");
        menu2.setSeq(2);

        SysMenuDef menu3 = new SysMenuDef();
        menu3.setId("sys-role");
        menu3.setName("角色管理");
        menu3.setPid("sys");
        menu3.setSeq(3);

        List<SysMenuDef> allMenus = Arrays.asList(menu1, menu2, menu3);

        when(roleRepository.findById("2")).thenReturn(Optional.of(normalRole));
        when(sysMenuRepository.findAll()).thenReturn(allMenus);
        when(sysMenuRepository.findAllById(Arrays.asList("sys", "sys-user"))).thenReturn(
            Arrays.asList(menu1, menu2)
        );

        List<SysMenuDef> result = sysRoleService.ownMenu("2");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sysMenuRepository, never()).findAll();
        verify(sysMenuRepository, times(1)).findAllById(Arrays.asList("sys", "sys-user"));
    }

    @Test
    void testOwnMenu_whenRoleHasNoMenus_shouldReturnEmptyList() {
        SysRole normalRole = new SysRole();
        normalRole.setId("3");
        normalRole.setCode("emptymenu");
        normalRole.setBuiltin(false);
        normalRole.setMenus(Arrays.asList());

        when(roleRepository.findById("3")).thenReturn(Optional.of(normalRole));
        when(sysMenuRepository.findAllById(Arrays.asList())).thenReturn(Arrays.asList());

        List<SysMenuDef> result = sysRoleService.ownMenu("3");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testOwnMenu_withMultipleRoles() {
        SysRole role1 = new SysRole();
        role1.setId("1");
        role1.setCode("admin");
        role1.setBuiltin(true);

        SysRole role2 = new SysRole();
        role2.setId("2");
        role2.setCode("normal");
        role2.setBuiltin(false);
        role2.setMenus(Arrays.asList("sys"));

        SysMenuDef menu1 = new SysMenuDef();
        menu1.setId("sys");
        menu1.setName("系统管理");

        when(roleRepository.findById("1")).thenReturn(Optional.of(role1));
        when(roleRepository.findById("2")).thenReturn(Optional.of(role2));
        when(sysMenuRepository.findAll()).thenReturn(Arrays.asList(menu1));

        Set<SysRole> roles = new HashSet<>(Arrays.asList(role1, role2));
        List<SysMenuDef> result = sysRoleService.ownMenu(roles);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindByCode_whenExists() {
        SysRole role = new SysRole();
        role.setId("1");
        role.setCode("admin");

        when(roleRepository.findByCode("admin")).thenReturn(Optional.of(role));

        Optional<SysRole> result = sysRoleService.findByCode("admin");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getCode());
    }

    @Test
    void testFindByCode_whenNotExists() {
        when(roleRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

        Optional<SysRole> result = sysRoleService.findByCode("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void testIsAdminRole() {
        SysRole adminRole = new SysRole();
        adminRole.setCode("admin");
        assertTrue(adminRole.isAdmin());

        SysRole normalRole = new SysRole();
        normalRole.setCode("normal");
        assertFalse(normalRole.isAdmin());
    }

    @Test
    void testInitDefaultAdmin_whenNotExists() {
        when(roleRepository.findByCode("admin")).thenReturn(Optional.empty());
        when(roleRepository.save(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId("1");
            return role;
        });

        SysRole result = sysRoleService.initDefaultAdmin();

        assertNotNull(result);
        assertEquals("admin", result.getCode());
        assertEquals("管理员", result.getName());
        assertTrue(result.getBuiltin());
        verify(roleRepository, times(1)).save(any(SysRole.class));
    }

    @Test
    void testInitDefaultAdmin_whenExists() {
        SysRole existingRole = new SysRole();
        existingRole.setId("1");
        existingRole.setCode("admin");
        existingRole.setName("管理员");

        when(roleRepository.findByCode("admin")).thenReturn(Optional.of(existingRole));

        SysRole result = sysRoleService.initDefaultAdmin();

        assertNotNull(result);
        assertEquals("admin", result.getCode());
        verify(roleRepository, never()).save(any(SysRole.class));
    }

    @Test
    void testSavePerms_shouldSetMenusAndPerms() {
        SysRole role = new SysRole();
        role.setId("1");
        role.setCode("test");
        role.setBuiltin(false);

        SysMenuDef menu1 = new SysMenuDef();
        menu1.setId("sys");
        menu1.setName("系统管理");

        SysMenuDef menu2 = new SysMenuDef();
        menu2.setId("sys-user");
        menu2.setName("用户管理");
        menu2.setPid("sys");

        when(sysMenuRepository.findAll()).thenReturn(Arrays.asList(menu1, menu2));
        when(roleRepository.findById("1")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(SysRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> perms = Arrays.asList("query", "save");
        List<String> menus = Arrays.asList("sys-user");

        SysRole result = sysRoleService.savePerms("1", perms, menus);

        assertNotNull(result);
        assertEquals(perms, result.getPerms());
        assertNotNull(result.getMenus());
        assertTrue(result.getMenus().contains("sys"));
        assertTrue(result.getMenus().contains("sys-user"));
    }
}
