package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.security.PermissionStaleService;
import io.github.jiangood.openadmin.modules.system.dto.converter.UserConverter;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterPermVO;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysUserServicePermViewTest {

    @Mock
    private SysUserRepository sysUserRepository;
    @Mock
    private SysRoleRepository roleRepository;
    @Mock
    private SysOrgService sysOrgService;
    @Mock
    private SysMenuRepository sysMenuRepository;
    @Mock
    private UserConverter userConverter;
    @Mock
    private PermissionStaleService permissionStaleService;

    private SysUserService sysUserService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        sysUserService = new SysUserService(sysUserRepository, roleRepository, sysOrgService,
                sysMenuRepository, userConverter, permissionStaleService, passwordEncoder);
    }

    private MenuDefinition menuWithPerms() {
        MenuDefinition menu = new MenuDefinition();
        menu.setId("sys-user");
        menu.setName("用户管理");
        MenuDefinition.PermDefinition read = new MenuDefinition.PermDefinition();
        read.setName("读取");
        read.setCode("read");
        MenuDefinition.PermDefinition edit = new MenuDefinition.PermDefinition();
        edit.setName("编辑");
        edit.setCode("edit");
        menu.setPerms(List.of(read, edit));
        return menu;
    }

    private SysOrg org(String id, String name, String pid) {
        SysOrg o = new SysOrg();
        o.setId(id);
        o.setName(name);
        o.setPid(pid);
        return o;
    }

    @Test
    void getPermView_admin_allScope_allPerms() {
        SysUser user = new SysUser();
        user.setId("u1");
        user.setAccount("admin");
        user.setDataPermType(DataPermType.ALL);
        user.setUnitId("unit1");
        user.setOrgId("org1");
        SysRole admin = new SysRole();
        admin.setCode("admin");
        admin.setName("管理员");
        user.setRoles(new HashSet<>(Set.of(admin)));

        when(sysUserRepository.findById("u1")).thenReturn(Optional.of(user));
        when(sysOrgService.findAll()).thenReturn(List.of(org("unit1", "总部", null), org("org1", "研发部", "unit1")));
        when(sysMenuRepository.findAll()).thenReturn(List.of(menuWithPerms()));

        UserCenterPermVO vo = sysUserService.getPermView("u1");

        assertNotNull(vo);
        assertEquals("ALL", vo.getDataPermType());
        assertEquals("unit1", vo.getUnitId());
        assertEquals("org1", vo.getOrgId());
        assertEquals(1, vo.getRoles().size());
        assertEquals("管理员", vo.getRoles().get(0).getName());
        assertEquals("admin", vo.getRoles().get(0).getCode());
        // ALL => 全量机构 id
        assertEquals(Set.of("unit1", "org1"), Set.copyOf(vo.getOrgPermIds()));
        // admin => 全量权限码
        assertTrue(vo.getOwnedPerms().containsAll(List.of("sys-user:read", "sys-user:edit")));
        // 菜单树根节点下权限叶子 key 为完整权限码
        assertEquals(1, vo.getMenuTree().size());
        TreeOption menuNode = vo.getMenuTree().get(0);
        assertEquals("sys-user", menuNode.getKey());
        List<String> leafKeys = menuNode.getChildren().stream().map(TreeOption::getKey).toList();
        assertTrue(leafKeys.containsAll(List.of("sys-user:read", "sys-user:edit")));
        // 机构树
        assertEquals(1, vo.getOrgTree().size());
        assertEquals("unit1", vo.getOrgTree().get(0).getKey());
    }

    @Test
    void getPermView_customScope_operatorRole() {
        SysUser user = new SysUser();
        user.setId("u2");
        user.setAccount("op");
        user.setDataPermType(DataPermType.CUSTOM);
        user.setUnitId("unit1");
        SysRole operator = new SysRole();
        operator.setCode("operator");
        operator.setName("运营");
        operator.setPerms(List.of("sys-user:read"));
        user.setRoles(new HashSet<>(Set.of(operator)));
        user.setDataPerms(List.of(org("org2", "运营部", null)));

        when(sysUserRepository.findById("u2")).thenReturn(Optional.of(user));
        when(sysOrgService.findAll()).thenReturn(List.of(org("unit1", "总部", null)));
        when(sysMenuRepository.findAll()).thenReturn(List.of(menuWithPerms()));

        UserCenterPermVO vo = sysUserService.getPermView("u2");

        assertEquals("CUSTOM", vo.getDataPermType());
        assertEquals(List.of("org2"), vo.getOrgPermIds());
        assertEquals(List.of("sys-user:read"), vo.getOwnedPerms());
        assertEquals(1, vo.getRoles().size());
        assertEquals("运营", vo.getRoles().get(0).getName());
        // 菜单树即使无权限也应展示全量菜单，权限叶子完整
        assertEquals(1, vo.getMenuTree().size());
        assertEquals(2, vo.getMenuTree().get(0).getChildren().size());
    }
}
