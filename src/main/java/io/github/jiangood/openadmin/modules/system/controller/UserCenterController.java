package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.UpdatePwdReq;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterInfo;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/userCenter")
@RequiredArgsConstructor
public class UserCenterController {

    private final SysUserService sysUserService;


    @RequestMapping("info")
    public AjaxResult info() {
        LoginUser sysUser = LoginTool.getUser();

        UserVO user = sysUserService.findOneDto(sysUser.getId());

        UserCenterInfo info = new UserCenterInfo();
        info.setName(sysUser.getName());
        info.setOrg(user.getOrgLabel());
        info.setEmail(user.getEmail());
        info.setAccount(user.getAccount());
        info.setPhone(user.getPhone());
        info.setRoles(user.getRoleNames());
        info.setUnit(user.getUnitLabel());
        info.setCreateTime(user.getCreateTime());

        return AjaxResult.ok().data(info);
    }


    @PostMapping("update-pwd")
    public AjaxResult updatePwd(@RequestBody UpdatePwdReq request, HttpServletRequest servletRequest) {
        sysUserService.updatePwd(LoginTool.getUserId(), request.getOldPassword(), request.getNewPassword());
        SecurityContextHolder.clearContext();
        servletRequest.getSession().invalidate();
        return AjaxResult.ok();
    }
}
