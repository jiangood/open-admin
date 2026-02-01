package io.github.jiangood.openadmin.framework.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class LoginUser extends User {

    private String id;
    private String name;
    private String unitId;
    private String unitName;
    private String deptId;
    private String deptName;

    private String deptLeaderId;

    public LoginUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }


}
