package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class SysUserDetailsService implements UserDetailsService {

    private SysUserService userService;
    private SysOrgService sysOrgService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("loadUserByUsername {}", username);
        SysUser user = userService.findByAccount(username).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UsernameNotFoundException("用户被禁用: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<String> permissions = userService.getUserPerms(user.getId());
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        LoginUser loginUser = new LoginUser(user.getAccount(), user.getPassword(), authorities);

        UserVO dto = userService.findOneDto(user.getId());
        loginUser.setId(dto.getId());
        loginUser.setOrgId(dto.getOrgId());
        loginUser.setOrgName(dto.getOrgLabel());
        loginUser.setUnitId(dto.getUnitId());
        loginUser.setUnitName(dto.getUnitLabel());
        loginUser.setName(dto.getName());

        SysUser deptLeader = sysOrgService.getDeptLeader(user.getId());
        if (deptLeader != null) {
            log.debug("登录用户 {} 的上级领导为：{}", user.getName(), deptLeader.getId());
            loginUser.setDeptLeaderId(deptLeader.getId());
        } else {
            log.debug("登录用户 {} 无上级领导", user.getName());
        }

        return loginUser;
    }
}
