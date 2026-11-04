package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.auth.online.OnlineUser;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUserService;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.modules.system.service.UserShortcutService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserCenterSessionControllerTest {

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

    @Test
    void sessions_shouldReturnCurrentUsersSessionsOnly() throws Exception {
        OnlineUser row = new OnlineUser();
        row.setId("s1");
        row.setIp("10.0.0.8");
        row.setInnerIp(true);
        row.setUserAgent("JUnit Browser");
        row.setLoginTime(LocalDateTime.of(2026, 11, 2, 9, 12));
        row.setLastActiveTime(LocalDateTime.of(2026, 11, 2, 10, 3));
        row.setCurrent(true);

        when(onlineUserService.listByUser("u1")).thenReturn(List.of(row));

        mockMvc.perform(get("/admin/user-center/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("s1"))
                .andExpect(jsonPath("$.data[0].ip").value("10.0.0.8"))
                .andExpect(jsonPath("$.data[0].innerIp").value(true))
                .andExpect(jsonPath("$.data[0].current").value(true))
                .andExpect(jsonPath("$.data[0].userId").doesNotExist());

        verify(onlineUserService).listByUser("u1");
    }

    @Test
    void kickSession_shouldKickOnlyCurrentUsersSession() throws Exception {
        mockMvc.perform(post("/admin/user-center/sessions/kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"s1\"}"))
                .andExpect(status().isOk());

        verify(onlineUserService).kickOwn("s1", "u1");
    }
}
