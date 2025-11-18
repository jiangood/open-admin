package io.admin.modules.flowable.core.assignment.impl;

import io.admin.modules.flowable.core.assignment.AssignmentTypeProvider;
import io.admin.modules.flowable.core.assignment.Identity;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.SysUserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseUserProvider implements AssignmentTypeProvider {

    @Resource
    SysUserService sysUserService;

    @Override
    public Collection<Identity> findAll() {
        List<SysUser> list = sysUserService.findAll();

        List<Identity> result = new ArrayList<>();
        for (SysUser entry : list) {
            Identity identity = new Identity(entry.getId(), null, entry.getName(), true, true);
            result.add(identity);
        }

        return result;
    }

    @Override
    public String getLabelById(String id) {
        if(id.contains(",")){
            String[] ids = id.split(",");
            StringBuilder sb = new StringBuilder();
            for(String str: ids){
                if(StringUtils.isNotEmpty(str)){

                    String name = sysUserService.getNameById(str);
                    sb.append(name).append("、");
                }
            }
            if(!sb.isEmpty()){
                sb.deleteCharAt(sb.length()-1);
            }
            return sb.toString();
        }
        return sysUserService.getNameById(id);
    }


}
