package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.UpdatePwdRequest;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterInfo;
import io.github.jiangood.openadmin.modules.system.dto.response.UserResponse;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/userCenter")
public class UserCenterController {

    @Resource
    private SysUserService sysUserService;


    @RequestMapping("info")
    public AjaxResult info() {
        LoginUser sysUser = LoginTool.getUser();

        UserResponse user = sysUserService.findOneDto(sysUser.getId());

        UserCenterInfo info = new UserCenterInfo();
        info.setName(sysUser.getName());
        info.setDept(user.getDeptLabel());
        info.setEmail(user.getEmail());
        info.setAccount(user.getAccount());
        info.setPhone(user.getPhone());
        info.setRoles(user.getRoleNames());
        info.setUnit(user.getUnitLabel());
        info.setCreateTime(user.getCreateTime());

        return AjaxResult.ok().data(info);
    }


    @PostMapping("updatePwd")
    public AjaxResult updatePwd(@RequestBody UpdatePwdRequest request) {
        String newPassword = request.getNewPassword();
        sysUserService.updatePwd(LoginTool.getUserId(), newPassword);
        return AjaxResult.ok();
    }
}
