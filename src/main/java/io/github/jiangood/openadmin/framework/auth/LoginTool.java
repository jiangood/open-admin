package io.github.jiangood.openadmin.framework.auth;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

@Slf4j
public class LoginTool {
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
        User principal = getUser();
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
        User principal = getUser();
        if (principal == null) {
            return List.of();
        }
        Collection<GrantedAuthority> authorities = principal.getAuthorities();

        return authorities.stream().map(GrantedAuthority::getAuthority)
                .filter(t -> !t.startsWith("ROLE_") && !t.startsWith("ORG_"))
                .toList();
    }

    public static List<String> getRoles() {
        User principal = getUser();
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
}
