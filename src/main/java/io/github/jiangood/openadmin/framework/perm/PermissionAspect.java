package io.github.jiangood.openadmin.framework.perm;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static io.github.jiangood.openadmin.lang.dto.CommonMessage.FORBIDDEN_MESSAGE;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(hasPermission)")
    public void checkPermission(HasPermission hasPermission) throws Throwable {
        String permission = hasPermission.value();

        if (!hasPermission(permission)) {
            throw new AccessDeniedException(FORBIDDEN_MESSAGE + "：" + permission);
        }
    }

    private boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 检查用户是否拥有指定权限
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(permission));
    }
}
