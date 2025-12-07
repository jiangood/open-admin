package io.admin.modules.system.dto.mapper;

import io.admin.modules.system.dao.SysOrgDao;
import io.admin.modules.system.dto.response.UserResponse;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import jakarta.annotation.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Resource
    SysOrgDao orgDao;


    @Mapping(target = "unitLabel", source = "unitId", qualifiedByName = "getOrgName")
    @Mapping(target = "deptLabel", source = "deptId", qualifiedByName = "getOrgName")
    @Mapping(target = "roleNames", source = "roles", qualifiedByName = "getRoleNames")
    public abstract UserResponse toResponse(SysUser input);

    public abstract List<UserResponse> toResponse(List<SysUser> input);

    @Named("getOrgName")
    protected String getOrgName(String orgId) {
        return orgDao.getNameById(orgId);
    }

    @Named("getRoleNames")
    protected List<String> getRoleNames(Set<SysRole> roles) {
        return roles.stream().map(SysRole::getName).toList();
    }


}
