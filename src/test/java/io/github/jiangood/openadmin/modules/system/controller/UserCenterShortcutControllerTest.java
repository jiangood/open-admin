package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.auth.online.OnlineUserService;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterShortcutVO;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.modules.system.service.UserShortcutService;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserCenterShortcutControllerTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysLogService sysLogService;

    @Mock
    private OnlineUserService onlineUserService;

    @Mock
    private UserShortcutService userShortcutService;

    @InjectMocks
    private UserCenterController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        LoginUser loginUser = new LoginUser("admin", "",
                List.of(new SimpleGrantedAuthority("ROLE_admin")));
        loginUser.setId("u1");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserCenterShortcutVO shortcut() {
        UserCenterShortcutVO vo = new UserCenterShortcutVO();
        vo.setId("sys-user");
        vo.setName("用户管理");
        vo.setPath("/system/user");
        vo.setIcon("UserOutlined");
        return vo;
    }

    @Test
    void shortcuts_shouldReturnCurrentUsersList() throws Exception {
        when(userShortcutService.list("u1")).thenReturn(List.of(shortcut()));

        mockMvc.perform(get("/admin/user-center/shortcuts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("sys-user"))
                .andExpect(jsonPath("$.data[0].path").value("/system/user"));

        verify(userShortcutService).list("u1");
    }

    @Test
    void shortcutOptions_shouldReturnCurrentUsersTree() throws Exception {
        TreeOption root = new TreeOption("系统管理", "sys", null);
        root.setDisableCheckbox(true);
        TreeOption child = new TreeOption("用户管理", "sys-user", "sys");
        child.setLeaf(true);
        root.setChildren(List.of(child));

        when(userShortcutService.options("u1")).thenReturn(List.of(root));

        mockMvc.perform(get("/admin/user-center/shortcut-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value("sys"))
                .andExpect(jsonPath("$.data[0].children[0].key").value("sys-user"));

        verify(userShortcutService).options("u1");
    }

    @Test
    void saveShortcuts_shouldPassMenuIdsInOrder() throws Exception {
        when(userShortcutService.save("u1", List.of("sys-role", "sys-user")))
                .thenReturn(List.of(shortcut()));

        mockMvc.perform(post("/admin/user-center/shortcuts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[\"sys-role\",\"sys-user\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("sys-user"));

        verify(userShortcutService).save("u1", List.of("sys-role", "sys-user"));
    }
}
