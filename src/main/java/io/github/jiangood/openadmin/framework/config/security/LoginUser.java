package io.github.jiangood.openadmin.framework.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class LoginUser extends User { // NOSONAR: 继承 Spring User 的 equals（按 username），无需自定义

    private String id;
    private String name;
    private String unitId;
    private String unitName;
    private String orgId;
    private String orgName;

    private String deptLeaderId;

    private final Set<String> permissions;

    public LoginUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.permissions = toPermissions(authorities);
    }


    public LoginUser(String username, String password,
                     boolean enabled,
                     boolean accountNonExpired,
                     boolean credentialsNonExpired,
                     boolean accountNonLocked,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.permissions = toPermissions(authorities);
    }

    private static Set<String> toPermissions(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
    }
}
