package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterPermVO;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserCenterPermControllerTest {

    @Mock
    private SysUserService sysUserService;

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
    void perms_shouldReturnPermView() throws Exception {
        UserCenterPermVO vo = new UserCenterPermVO();
        vo.setDataPermLabel("所有");

        when(sysUserService.getPermView("u1")).thenReturn(vo);

        mockMvc.perform(get("/admin/userCenter/perms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataPermLabel").value("所有"));
    }
}
