package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.modules.common.LoginTool;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

// 审计者实现, @CreatedBy 等字段才会生效
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @NotNull
    @Override
    public Optional<String> getCurrentAuditor() {
        LoginUser user = LoginTool.getUser();
        if (user != null && user.getId() != null) {
            return Optional.of(user.getId());
        }
        return Optional.empty();
    }
}
