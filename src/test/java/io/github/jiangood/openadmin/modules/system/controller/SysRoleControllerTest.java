package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.service.SysMenuService;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SysRoleControllerTest {

    @Mock
    private SysRoleService sysRoleService;
    @Mock
    private SysMenuService sysMenuService;
    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private SysRoleController controller;

    private MockMvc mockMvc;

    @Test
    void ownPerms_whenRoleHasWildcard_shouldReturnAllMenuPerms() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        SysRole admin = new SysRole();
        admin.setId("1");
        admin.setCode("admin");
        admin.setPerms(List.of("*"));

        MenuDefinition menu = new MenuDefinition();
        menu.setId("sys-user");
        menu.setName("用户管理");
        menu.setPerms(List.of(
                new MenuDefinition.PermDefinition() {{
                    setName("读取");
                    setCode("read");
                }},
                new MenuDefinition.PermDefinition() {{
                    setName("删除");
                    setCode("delete");
                }}
        ));

        when(sysRoleService.findById("1")).thenReturn(Optional.of(admin));
        when(sysRoleService.ownMenu("1")).thenReturn(List.of(menu));

        mockMvc.perform(get("/admin/sysRole/own-perms").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['sys-user']").isArray())
                .andExpect(jsonPath("$.data['sys-user'][0]").value("sys-user:read"))
                .andExpect(jsonPath("$.data['sys-user'][1]").value("sys-user:delete"));
    }

    @Test
    void ownPerms_whenRoleHasConcretePerms_shouldFilter() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        SysRole role = new SysRole();
        role.setId("2");
        role.setCode("normal");
        role.setPerms(List.of("sys-user:read"));

        MenuDefinition menu = new MenuDefinition();
        menu.setId("sys-user");
        menu.setName("用户管理");
        menu.setPerms(List.of(
                new MenuDefinition.PermDefinition() {{
                    setName("读取");
                    setCode("read");
                }},
                new MenuDefinition.PermDefinition() {{
                    setName("删除");
                    setCode("delete");
                }}
        ));

        when(sysRoleService.findById("2")).thenReturn(Optional.of(role));
        when(sysRoleService.ownMenu("2")).thenReturn(List.of(menu));

        mockMvc.perform(get("/admin/sysRole/own-perms").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['sys-user']").isArray())
                .andExpect(jsonPath("$.data['sys-user'][0]").value("sys-user:read"))
                .andExpect(jsonPath("$.data['sys-user'].length()").value(1));
    }
}
