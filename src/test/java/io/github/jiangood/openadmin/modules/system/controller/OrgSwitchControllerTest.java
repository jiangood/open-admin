package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrgSwitchControllerTest {

    @Mock
    private SysOrgService sysOrgService;

    @InjectMocks
    private OrgSwitchController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        LoginUser loginUser = new LoginUser("admin", "",
                List.of(new SimpleGrantedAuthority("ROLE_admin"), new SimpleGrantedAuthority("ORG_org1")));
        loginUser.setId("u1");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SysOrg unit(String id, String name) {
        SysOrg org = new SysOrg();
        org.setId(id);
        org.setName(name);
        org.setPid(null);
        org.setType(1);
        org.setEnabled(true);
        return org;
    }

    @Test
    void myOrgs_shouldReturnUnitTree() throws Exception {
        when(sysOrgService.findByLoginUser(1)).thenReturn(List.of(unit("org1", "默认单位")));

        mockMvc.perform(get("/admin/myOrgs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tree[0].key").value("org1"))
                .andExpect(jsonPath("$.data.tree[0].title").value("默认单位"));
    }

    @Test
    void switchOrg_shouldSucceedWhenAccessible() throws Exception {
        when(sysOrgService.findByLoginUser(1)).thenReturn(List.of(unit("org1", "默认单位")));

        mockMvc.perform(post("/admin/switchOrg")
                        .contentType("application/json")
                        .content("{\"orgId\":\"org1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void switchOrg_shouldRejectWhenNotAccessible() throws Exception {
        when(sysOrgService.findByLoginUser(1)).thenReturn(List.of());

        mockMvc.perform(post("/admin/switchOrg")
                        .contentType("application/json")
                        .content("{\"orgId\":\"org9\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
