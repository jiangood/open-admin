package io.github.jiangood.openadmin.framework.auth;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.modules.system.provider.UnitOrgTypeProvider;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.util.SpringTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.List;

@Slf4j
public class LoginTool {
    private LoginTool() {
    }


    private static final String SESSION_ORG_ID = "currentOrgId";

    public static String getUserId() {
        LoginUser user = getUser();
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    public static LoginUser getUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }
        String account = authentication.getName();
        log.debug("获取当前用户 {}", account);
        boolean authenticated = authentication.isAuthenticated();
        if (!authenticated || "anonymousUser".equals(account)) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if(principal instanceof LoginUser loginUser){
            return loginUser;
        }
        return null;
    }

    public static List<String> getOrgPermissions() {
        LoginUser principal = getUser();
        if (principal == null) {
            return List.of();
        }
        Collection<GrantedAuthority> authorities = principal.getAuthorities();

        return authorities.stream().map(GrantedAuthority::getAuthority)
                .filter(t -> t.startsWith("ORG_"))
                .map(t -> t.substring(4))
                .toList();
    }

    public static List<String> getPermissions() {
        LoginUser principal = getUser();
        if (principal == null) {
            return List.of();
        }
        Collection<GrantedAuthority> authorities = principal.getAuthorities();

        return authorities.stream().map(GrantedAuthority::getAuthority)
                .filter(t -> !t.startsWith("ROLE_") && !t.startsWith("ORG_"))
                .toList();
    }

    public static List<String> getRoles() {
        LoginUser principal = getUser();
        if (principal == null) {
            return List.of();
        }
        Collection<GrantedAuthority> authorities = principal.getAuthorities();

        return authorities.stream().map(GrantedAuthority::getAuthority)
                .filter(t -> t.startsWith("ROLE_"))
                .map(t -> t.substring(5))
                .toList();
    }

    public static boolean isAdmin() {
        List<String> roles = getRoles();
        return roles.contains("admin");
    }

    /**
     * 当前组织机构 id：session 优先；仅有一个可访问单位时自动选中。
     */
    public static String getCurrentOrgId() {
        String sessionOrgId = getSessionOrgId();
        if (sessionOrgId != null) {
            return sessionOrgId;
        }
        List<String> unitIds = getAccessibleUnitIds();
        if (unitIds.size() == 1) {
            String orgId = unitIds.get(0);
            setCurrentOrgId(orgId);
            return orgId;
        }
        if (unitIds.size() > 1) {
            log.info("当前用户有多个组织机构，请选择组织机构");
        }
        return null;
    }

    public static void setCurrentOrgId(String orgId) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(SESSION_ORG_ID, orgId);
        }
    }

    private static List<String> getAccessibleUnitIds() {
        List<String> orgPerms = getOrgPermissions();
        if (orgPerms == null || orgPerms.isEmpty()) {
            return List.of();
        }
        SysOrgService sysOrgService = SpringTool.getBean(SysOrgService.class);
        if (sysOrgService == null) {
            return List.of();
        }
        return sysOrgService.findByLoginUser(UnitOrgTypeProvider.TYPE_UNIT)
                .stream()
                .map(BaseEntity::getId)
                .toList();
    }

    private static String getSessionOrgId() {
        HttpSession session = getSession();
        if (session != null) {
            return (String) session.getAttribute(SESSION_ORG_ID);
        }
        return null;
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            if (request != null) {
                return request.getSession();
            }
        }
        return null;
    }
}
