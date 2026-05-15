package io.github.jiangood.openadmin.framework.perm;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

import static io.github.jiangood.openadmin.framework.MessageConst.MGS_FORBIDDEN;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(hasPermission)")
    public void checkPermission(HasPermission hasPermission) throws Throwable {
        String permission = hasPermission.value();

        if (!hasPermission(permission)) {
            throw new AccessDeniedException(MGS_FORBIDDEN + "：" + permission);
        }
    }

    private boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (authentication.getPrincipal() instanceof LoginUser loginUser) {
            Set<String> permissions = loginUser.getPermissions();
            if (permissions.contains("*")) {
                return true;
            }
            return permissions.contains(permission);
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(permission));
    }
}
