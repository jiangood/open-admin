package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.UpdatePwdReq;
import io.github.jiangood.openadmin.modules.system.dto.request.UpdateUserCenterReq;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterInfo;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/userCenter")
@RequiredArgsConstructor
public class UserCenterController {

    private final SysUserService sysUserService;


    @GetMapping("info")
    public AjaxResult info() {
        LoginUser sysUser = LoginTool.getUser();

        UserVO user = sysUserService.findOneDto(sysUser.getId());

        UserCenterInfo info = new UserCenterInfo();
        info.setName(sysUser.getName());
        info.setOrg(user.getOrgLabel());
        info.setEmail(user.getEmail());
        info.setAccount(user.getAccount());
        info.setPhone(user.getPhone());
        info.setAvatar(user.getAvatar());
        info.setRoles(user.getRoleNames());
        info.setUnit(user.getUnitLabel());
        info.setCreateTime(user.getCreateTime());

        return AjaxResult.ok().data(info);
    }


    @GetMapping("perms")
    public AjaxResult perms() {
        return AjaxResult.ok().data(sysUserService.getPermView(LoginTool.getUserId()));
    }


    /**
     * 修改个人资料（邮箱、头像）
     */
    @PostMapping("update-info")
    public AjaxResult updateInfo(@Valid @RequestBody UpdateUserCenterReq request) {
        sysUserService.updateInfo(LoginTool.getUserId(), request.getEmail(), request.getAvatar());
        return AjaxResult.ok("保存成功");
    }


    @PostMapping("update-pwd")
    public AjaxResult updatePwd(@RequestBody UpdatePwdReq request, HttpServletRequest servletRequest) {
        sysUserService.updatePwd(LoginTool.getUserId(), request.getOldPassword(), request.getNewPassword());
        SecurityContextHolder.clearContext();
        servletRequest.getSession().invalidate();
        return AjaxResult.ok();
    }
}
