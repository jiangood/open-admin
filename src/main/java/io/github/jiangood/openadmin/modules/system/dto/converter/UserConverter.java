package io.github.jiangood.openadmin.modules.system.dto.converter;

import cn.hutool.core.bean.BeanUtil;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserConverter {

    private final SysOrgService sysOrgService;

    public UserConverter(SysOrgService sysOrgService) {
        this.sysOrgService = sysOrgService;
    }

    public UserVO toResponse(SysUser input) {
        UserVO r = new UserVO();
        BeanUtil.copyProperties(input, r);
        r.setUnitLabel(sysOrgService.getNameById(input.getUnitId()));
        r.setOrgLabel(sysOrgService.getNameById(input.getOrgId()));
        r.setRoleNames(input.getRoles().stream().map(SysRole::getName).toList());
        return r;
    }

    public List<UserVO> toResponse(List<SysUser> list) {
        return list.stream().map(this::toResponse).toList();
    }
}
