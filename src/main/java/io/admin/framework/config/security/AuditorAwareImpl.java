package io.admin.framework.config.security;

import io.admin.modules.common.LoginUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

// 审计者实现, @CreatedBy 等字段才会生效
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @NotNull
    @Override
    public Optional<String> getCurrentAuditor() {
        LoginUser user = LoginUtils.getUser();
        if(user != null && user.getId() != null){
            return Optional.of(user.getId());
        }
        return Optional.empty();
    }
}
