package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
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
    private jakarta.persistence.EntityManager entityManager;

    @Mock
    private SysMenuRepository sysMenuRepository;

    @Mock
    private SysUserRepository sysUserRepository;

    private SysRoleService sysRoleService;

    @BeforeEach
    void setUp() {
        sysRoleService = new SysRoleService(roleRepository, entityManager, roleRepository, sysMenuRepository, sysUserRepository);
    }

    @Test
    void testOwnMenu_whenRoleIsAdmin_shouldReturnAllMenus() {
        SysRole adminRole = new SysRole();
        adminRole.setId("1");
        adminRole.setCode("admin");
        MenuDefinition menu1 = new MenuDefinition();
        menu1.setId("sys");
        menu1.setName("系统管理");
        menu1.setSeq(1);

        MenuDefinition menu2 = new MenuDefinition();
        menu2.setId("sys-user");
        menu2.setName("用户管理");
        menu2.setPid("sys");
        menu2.setSeq(2);

        List<MenuDefinition> allMenus = Arrays.asList(menu1, menu2);

        when(roleRepository.findById("1")).thenReturn(Optional.of(adminRole));
        when(sysMenuRepository.findAll()).thenReturn(allMenus);

        List<MenuDefinition> result = sysRoleService.ownMenu("1");

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
        normalRole.setMenus(Arrays.asList("sys", "sys-user"));

        MenuDefinition menu1 = new MenuDefinition();
        menu1.setId("sys");
        menu1.setName("系统管理");
        menu1.setSeq(1);

        MenuDefinition menu2 = new MenuDefinition();
        menu2.setId("sys-user");
        menu2.setName("用户管理");
        menu2.setPid("sys");
        menu2.setSeq(2);

        MenuDefinition menu3 = new MenuDefinition();
        menu3.setId("sys-role");
        menu3.setName("角色管理");
        menu3.setPid("sys");
        menu3.setSeq(3);

        when(roleRepository.findById("2")).thenReturn(Optional.of(normalRole));
        when(sysMenuRepository.findAllById(Arrays.asList("sys", "sys-user"))).thenReturn(
            Arrays.asList(menu1, menu2)
        );

        List<MenuDefinition> result = sysRoleService.ownMenu("2");

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
        normalRole.setMenus(Arrays.asList());

        when(roleRepository.findById("3")).thenReturn(Optional.of(normalRole));

        List<MenuDefinition> result = sysRoleService.ownMenu("3");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testOwnMenu_whenRoleMenusIsNull_shouldReturnEmptyList() {
        SysRole normalRole = new SysRole();
        normalRole.setId("4");
        normalRole.setCode("normal");

        when(roleRepository.findById("4")).thenReturn(Optional.of(normalRole));

        List<MenuDefinition> result = sysRoleService.ownMenu("4");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sysMenuRepository, never()).findAllById(any());
    }

    @Test
    void testOwnMenu_withMultipleRoles() {
        SysRole role1 = new SysRole();
        role1.setId("1");
        role1.setCode("admin");
        SysRole role2 = new SysRole();
        role2.setId("2");
        role2.setCode("normal");
        role2.setMenus(Arrays.asList("sys"));

        MenuDefinition menu1 = new MenuDefinition();
        menu1.setId("sys");
        menu1.setName("系统管理");

        when(roleRepository.findById("1")).thenReturn(Optional.of(role1));
        when(roleRepository.findById("2")).thenReturn(Optional.of(role2));
        when(sysMenuRepository.findAll()).thenReturn(Arrays.asList(menu1));

        Set<SysRole> roles = new HashSet<>(Arrays.asList(role1, role2));
        List<MenuDefinition> result = sysRoleService.ownMenu(roles);

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
        MenuDefinition menu1 = new MenuDefinition();
        menu1.setId("sys");
        menu1.setName("系统管理");

        MenuDefinition menu2 = new MenuDefinition();
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

    @Test
    void testSavePerms_whenAdminRole_shouldKeepWildcardPerms() {
        SysRole role = new SysRole();
        role.setId("1");
        role.setCode("admin");
        role.setPerms(Arrays.asList("*"));

        MenuDefinition menu1 = new MenuDefinition();
        menu1.setId("sys");
        menu1.setName("系统管理");

        when(sysMenuRepository.findAll()).thenReturn(Arrays.asList(menu1));
        when(roleRepository.findById("1")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(SysRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SysRole result = sysRoleService.savePerms("1", Arrays.asList("sys-user:read"), Arrays.asList("sys"));

        assertNotNull(result);
        assertEquals(Arrays.asList("*"), result.getPerms());
    }
}
