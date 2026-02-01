package io.github.jiangood.openadmin.modules.system.dto.mapper;

import cn.hutool.core.bean.BeanUtil;
import io.github.jiangood.openadmin.modules.system.dao.SysOrgDao;
import io.github.jiangood.openadmin.modules.system.dto.response.UserResponse;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    @Resource
    SysOrgDao orgDao;


    public UserResponse toResponse(SysUser input) {
        UserResponse r = new UserResponse();
        BeanUtil.copyProperties(input, r);
        r.setUnitLabel(orgDao.getNameById(input.getUnitId()));
        r.setDeptLabel(orgDao.getNameById(input.getDeptId()));
        r.setRoleNames(input.getRoles().stream().map(SysRole::getName).toList());
        return r;
    }

    public List<UserResponse> toResponse(List<SysUser> list) {
        return list.stream().map(this::toResponse).toList();
    }


}
