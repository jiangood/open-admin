package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.datadefinition.DataProperties;
import io.github.jiangood.openadmin.framework.config.datadefinition.DataPropertiesFactory;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MenuLoadingIntegrationTest {

    @Autowired
    private SysMenuRepository sysMenuRepository;

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysRoleService sysRoleService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testDataPropertiesFactory_loadsMenus() {
        DataProperties props = DataPropertiesFactory.getInstance();
        List<MenuDefinition> menus = props.getMenus();

        assertNotNull(menus);
        assertFalse(menus.isEmpty(), "菜单列表不应该为空");

        System.out.println("加载的菜单数量: " + menus.size());
        menus.forEach(menu -> {
            System.out.println("菜单: " + menu.getId() + " - " + menu.getName());
        });
    }

    @Test
    void testSysMenuRepository_findAll() {
        List<MenuDefinition> menus = sysMenuRepository.findAll();

        assertNotNull(menus);
        assertFalse(menus.isEmpty(), "菜单列表不应该为空");

        System.out.println("SysMenuRepository.findAll() 返回的菜单数量: " + menus.size());
    }

    @Test
    void testSysMenuRepository_findAllById() {
        List<MenuDefinition> allMenus = sysMenuRepository.findAll();
        assertFalse(allMenus.isEmpty());

        List<String> menuIds = Arrays.asList(allMenus.get(0).getId());
        List<MenuDefinition> filteredMenus = sysMenuRepository.findAllById(menuIds);

        assertNotNull(filteredMenus);
        assertFalse(filteredMenus.isEmpty());
        assertEquals(1, filteredMenus.size());
        assertEquals(menuIds.get(0), filteredMenus.get(0).getId());
    }

    @Test
    void testSysRoleService_ownMenu_withAdminRole() {
        Optional<SysRole> adminRoleOpt = sysRoleRepository.findByCode("admin");

        if (adminRoleOpt.isEmpty()) {
            fail("admin角色不存在，请先运行初始化");
        }

        SysRole adminRole = adminRoleOpt.get();
        List<MenuDefinition> menus = sysRoleService.ownMenu(adminRole.getId());

        assertNotNull(menus);
        assertFalse(menus.isEmpty(), "admin角色的菜单列表不应该为空");

        System.out.println("admin角色的菜单数量: " + menus.size());
        menus.forEach(menu -> {
            System.out.println("  菜单: " + menu.getId() + " - " + menu.getName());
        });
    }

    @Test
    void testSysRoleService_ownMenu_withUserRoles() {
        Optional<SysUser> adminUserOpt = sysUserRepository.findByAccount("admin");

        if (adminUserOpt.isEmpty()) {
            fail("admin用户不存在，请先运行初始化");
        }

        SysUser adminUser = adminUserOpt.get();
        Set<SysRole> roles = adminUser.getRoles();

        assertNotNull(roles);
        assertFalse(roles.isEmpty(), "admin用户的角色列表不应该为空");

        System.out.println("admin用户的角色数量: " + roles.size());
        roles.forEach(role -> {
            System.out.println("  角色: " + role.getCode() + " - " + role.getName());
        });

        List<MenuDefinition> menus = sysRoleService.ownMenu(roles);

        assertNotNull(menus);
        assertFalse(menus.isEmpty(), "通过用户角色获取的菜单列表不应该为空");

        System.out.println("通过用户角色获取的菜单数量: " + menus.size());
    }

    @Test
    void testCreateNewRoleAndAssignMenus() {
        Optional<SysRole> adminRoleOpt = sysRoleRepository.findByCode("admin");
        if (adminRoleOpt.isEmpty()) {
            fail("admin角色不存在");
        }

        List<MenuDefinition> allMenus = sysMenuRepository.findAll();
        if (allMenus.isEmpty()) {
            fail("没有可用的菜单");
        }

        SysRole newRole = new SysRole();
        newRole.setCode("test_role_" + System.currentTimeMillis());
        newRole.setName("测试角色");
        newRole.setBuiltin(false);
        newRole.setEnabled(true);
        newRole.setPerms(Arrays.asList("query", "save"));

        List<String> menuIds = Arrays.asList(allMenus.get(0).getId());
        newRole.setMenus(menuIds);

        SysRole savedRole = sysRoleRepository.save(newRole);
        assertNotNull(savedRole.getId());

        List<MenuDefinition> menus = sysRoleService.ownMenu(savedRole.getId());
        assertNotNull(menus);
        assertFalse(menus.isEmpty());

        sysRoleRepository.deleteById(savedRole.getId());
    }

    @Test
    void testAdminRole_shouldHaveAllMenus() {
        Optional<SysRole> adminRoleOpt = sysRoleRepository.findByCode("admin");

        if (adminRoleOpt.isEmpty()) {
            fail("admin角色不存在");
        }

        SysRole adminRole = adminRoleOpt.get();
        assertTrue(adminRole.isAdmin(), "admin角色应该被识别为管理员");

        List<MenuDefinition> adminMenus = sysRoleService.ownMenu(adminRole.getId());
        List<MenuDefinition> allMenus = sysMenuRepository.findAll();

        assertEquals(allMenus.size(), adminMenus.size(),
            "admin角色应该拥有所有菜单");

        System.out.println("所有菜单数量: " + allMenus.size());
        System.out.println("admin角色菜单数量: " + adminMenus.size());
    }

    @Test
    void testNormalRole_withSpecificMenus() {
        Optional<SysRole> adminRoleOpt = sysRoleRepository.findByCode("admin");
        if (adminRoleOpt.isEmpty()) {
            fail("admin角色不存在");
        }

        List<MenuDefinition> allMenus = sysMenuRepository.findAll();
        if (allMenus.size() < 2) {
            fail("需要至少2个菜单来测试");
        }

        SysRole normalRole = new SysRole();
        normalRole.setCode("normal_test_" + System.currentTimeMillis());
        normalRole.setName("普通测试角色");
        normalRole.setBuiltin(false);
        normalRole.setEnabled(true);
        normalRole.setPerms(Arrays.asList("query"));

        normalRole.setMenus(Arrays.asList(allMenus.get(0).getId()));

        SysRole savedRole = sysRoleRepository.save(normalRole);

        List<MenuDefinition> roleMenus = sysRoleService.ownMenu(savedRole.getId());

        assertEquals(1, roleMenus.size(),
            "普通角色应该只有分配的一个菜单");
        assertEquals(allMenus.get(0).getId(), roleMenus.get(0).getId());

        sysRoleRepository.deleteById(savedRole.getId());
    }
}
