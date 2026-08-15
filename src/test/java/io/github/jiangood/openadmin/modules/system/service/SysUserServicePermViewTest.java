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

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        sysUserService = new SysUserService(sysUserRepository, entityManager, sysUserRepository, roleRepository, sysOrgService,
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
        assertEquals("所有", vo.getDataPermLabel());
        // ALL => 全量机构 id 已授权
        assertEquals(1, vo.getOrgRows().size());
        assertEquals("unit1", vo.getOrgRows().get(0).getKey());
        assertEquals("mine", vo.getOrgRows().get(0).getStatus());
        assertEquals(1, vo.getOrgRows().get(0).getChildren().size());
        assertEquals("mine", vo.getOrgRows().get(0).getChildren().get(0).getStatus());
        // admin => 全量权限码已拥有
        assertEquals(1, vo.getMenuRows().size());
        UserCenterPermVO.MenuRow menuRow = vo.getMenuRows().get(0);
        assertEquals("用户管理", menuRow.getTitle());
        assertEquals(List.of("读取", "编辑"), menuRow.getPerms());
        assertEquals("all", menuRow.getStatus());
    }

    @Test
    void getPermView_customScope_operatorRole() {        SysUser user = new SysUser();
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

        assertEquals("自定义", vo.getDataPermLabel());
        // 机构树仅 unit1，且为我的机构
        assertEquals(1, vo.getOrgRows().size());
        assertEquals("mine", vo.getOrgRows().get(0).getStatus());
        // 菜单树即使无权限也应展示全量菜单，权限名称完整
        assertEquals(1, vo.getMenuRows().size());
        assertEquals(List.of("读取", "编辑"), vo.getMenuRows().get(0).getPerms());
        // 仅拥有 sys-user:read => 部分授权
        assertEquals("partial", vo.getMenuRows().get(0).getStatus());
    }

    @Test
    void getPermView_withSubMenu_shouldNotNpe() {
        SysUser user = new SysUser();
        user.setId("u3");
        user.setAccount("admin2");
        user.setDataPermType(DataPermType.ALL);
        user.setUnitId("unit1");
        user.setOrgId("org1");
        SysRole admin = new SysRole();
        admin.setCode("admin");
        admin.setName("管理员");
        user.setRoles(new HashSet<>(Set.of(admin)));

        MenuDefinition parent = new MenuDefinition();
        parent.setId("sys-parent");
        parent.setName("系统管理");
        MenuDefinition child = new MenuDefinition();
        child.setId("sys-user");
        child.setName("用户管理");
        child.setPid("sys-parent");
        MenuDefinition.PermDefinition read = new MenuDefinition.PermDefinition();
        read.setName("读取");
        read.setCode("read");
        child.setPerms(List.of(read));

        when(sysUserRepository.findById("u3")).thenReturn(Optional.of(user));
        when(sysOrgService.findAll()).thenReturn(List.of(org("unit1", "总部", null)));
        when(sysMenuRepository.findAll()).thenReturn(List.of(parent, child));

        UserCenterPermVO vo = sysUserService.getPermView("u3");

        // 父菜单行（无权限叶子、有子菜单）不应 NPE
        assertEquals(1, vo.getMenuRows().size());
        UserCenterPermVO.MenuRow parentRow = vo.getMenuRows().get(0);
        assertEquals("系统管理", parentRow.getTitle());
        assertTrue(parentRow.getPerms().isEmpty());
        assertEquals(1, parentRow.getChildren().size());
        assertEquals(List.of("读取"), parentRow.getChildren().get(0).getPerms());
        assertEquals("all", parentRow.getChildren().get(0).getStatus());
    }
}
