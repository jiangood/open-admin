package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserCenterLogControllerTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysLogService sysLogService;

    @InjectMocks
    private UserCenterController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
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
    void logs_shouldReturnOnlyCurrentUserLogs() throws Exception {
        SysLog sysLog = new SysLog();
        sysLog.setId("log1");
        sysLog.setOperation("个人中心-修改资料");
        sysLog.setSuccess(true);
        sysLog.setDuration(12);
        sysLog.setIp("127.0.0.1");
        sysLog.setOperationTime(LocalDateTime.of(2026, 11, 2, 10, 0));

        when(sysLogService.pageByUser(eq("u1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sysLog), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/user-center/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].operation").value("个人中心-修改资料"))
                .andExpect(jsonPath("$.data.content[0].success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(sysLogService).pageByUser(eq("u1"), any(Pageable.class));
    }
}
